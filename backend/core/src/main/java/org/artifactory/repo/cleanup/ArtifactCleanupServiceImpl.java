/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.repo.cleanup;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.stats.StatsSearchControls;
import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.schedule.BaseTaskServiceDescriptorHandler;
import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskInterruptedException;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.service.StatsServiceImpl;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The main implementation of the clean-up service
 *
 * @author Noam Tenne
 */
@Service
@Reloadable(beanClass = InternalArtifactCleanupService.class,
        initAfter = {TaskService.class, InternalRepositoryService.class})
public class ArtifactCleanupServiceImpl implements InternalArtifactCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ArtifactCleanupServiceImpl.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private StatsServiceImpl statsService;

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private SearchService searchService;

    @Override
    public void init() {
        reload(null);
    }

    @Override
    public void reload(@Nullable CentralConfigDescriptor oldDescriptor) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        CleanupConfigDescriptor oldCleanupConfig = null;
        if (oldDescriptor != null) {
            oldCleanupConfig = oldDescriptor.getCleanupConfig();
        }
        new CleanupConfigDescriptorHandler(descriptor.getCleanupConfig(), oldCleanupConfig).reschedule();
    }

    @Override
    public void destroy() {
        new CleanupConfigDescriptorHandler(null, null).unschedule();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        //nop
    }

    @Override
    public String callManualArtifactCleanup(BasicStatusHolder statusHolder) {
        TaskService taskService = InternalContextHelper.get().getTaskService();
        taskService.checkCanStartManualTask(ArtifactCleanupJob.class, statusHolder);
        if (!statusHolder.isError()) {
            try {
                TaskBase task = TaskUtils.createManualTask(ArtifactCleanupJob.class, 0L);
                return taskService.startTask(task, true, true);
            } catch (Exception e) {
                statusHolder.error("Error scheduling manual artifact cleanup", e, log);
            }
        }
        return null;
    }

    static class CleanupConfigDescriptorHandler extends BaseTaskServiceDescriptorHandler<CleanupConfigDescriptor> {

        final List<CleanupConfigDescriptor> oldDescriptorHolder = Lists.newArrayList();
        final List<CleanupConfigDescriptor> newDescriptorHolder = Lists.newArrayList();

        CleanupConfigDescriptorHandler(CleanupConfigDescriptor newDesc, CleanupConfigDescriptor oldDesc) {
            if (newDesc != null) {
                newDescriptorHolder.add(newDesc);
            }
            if (oldDesc != null) {
                oldDescriptorHolder.add(oldDesc);
            }
        }

        @Override
        public String jobName() {
            return "Artifact Cleanup";
        }

        @Override
        public List<CleanupConfigDescriptor> getNewDescriptors() {
            return newDescriptorHolder;
        }

        @Override
        public List<CleanupConfigDescriptor> getOldDescriptors() {
            return oldDescriptorHolder;
        }

        @Override
        public Predicate<Task> getAllPredicate() {
            return new Predicate<Task>() {
                @Override
                public boolean apply(@Nullable Task input) {
                    return input == null || ArtifactCleanupJob.class.isAssignableFrom(input.getType());
                }
            };
        }

        @Override
        public Predicate<Task> getPredicate(@Nonnull CleanupConfigDescriptor descriptor) {
            return getAllPredicate();
        }

        @Override
        public void activate(@Nonnull CleanupConfigDescriptor descriptor, boolean manual) {
            TaskBase cleanupTask = TaskUtils.createCronTask(ArtifactCleanupJob.class, descriptor.getCronExp());
            InternalContextHelper.get().getTaskService().startTask(cleanupTask, manual, manual);
        }

        @Override
        public CleanupConfigDescriptor findOldFromNew(@Nonnull CleanupConfigDescriptor newDescriptor) {
            return oldDescriptorHolder.isEmpty() ? null : oldDescriptorHolder.get(0);
        }
    }

    @Override
    public void clean() {
        // flush the download statistics before starting the cleanup
        statsService.flushStats();
        List<LocalCacheRepoDescriptor> cachedRepoDescriptors = repositoryService.getCachedRepoDescriptors();
        for (LocalCacheRepoDescriptor cachedRepoDescriptor : cachedRepoDescriptors) {
            performCleanOnRepo(cachedRepoDescriptor);
        }
    }

    private void performCleanOnRepo(LocalCacheRepoDescriptor cachedRepoDescriptor) {
        String repoKey = cachedRepoDescriptor.getKey();
        long periodMillis = getHoursInMillis(
                cachedRepoDescriptor.getRemoteRepo().getUnusedArtifactsCleanupPeriodHours());
        if (periodMillis <= 0) {
            log.debug("Skipping auto-clean on repository '{}': period value is {}.", repoKey, periodMillis);
            return;
        }

        LocalRepo storingRepo = repositoryService.localOrCachedRepositoryByKey(repoKey);
        //Perform sanity checks
        if (storingRepo == null) {
            log.warn("Could not find the storing repository '{}' - auto-clean was not performed.", repoKey);
            return;
        }
        if (!storingRepo.isCache()) {
            log.warn("Cannot cleanup non-cache repository '{}'.", repoKey);
            return;
        }

        doClean(repoKey, periodMillis);
    }

    private void doClean(String repoKey, long periodMillis) {
        log.info("Auto-clean has begun on the repository '{}' with period of {} millis.", repoKey, periodMillis);

        //Calculate unused artifact expiry
        long expiryMillis = (System.currentTimeMillis() - periodMillis);

        //Perform a metadata search on the given repo. Look for artifacts that have lastDownloaded stats
        StatsSearchControls searchControls = new StatsSearchControls();
        searchControls.setLimitSearchResults(false);
        searchControls.addRepoToSearch(repoKey);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(expiryMillis);
        searchControls.setDownloadedSince(calendar);
        ItemSearchResults<StatsSearchResult> metadataSearchResults =
                searchService.searchArtifactsNotDownloadedSince(searchControls);

        int iterationCount = 0;
        int cleanedArtifactsCount = 0;
        for (StatsSearchResult searchResult : metadataSearchResults.getResults()) {
            if ((++iterationCount % 10 == 0) && TaskUtils.pauseOrBreak()) {
                throw new TaskInterruptedException();
            }
            RepoPath repoPath = searchResult.getItemInfo().getRepoPath();
            long lastDownloaded = searchResult.getLastDownloaded();

            //If the artifact wasn't downloaded within the expiry window, remove it
            if (expiryMillis > lastDownloaded) {
                try {
                    repositoryService.undeploy(repoPath,
                            false);  // no need for maven metadata calculation on cache repos
                    cleanedArtifactsCount++;
                } catch (Exception e) {
                    log.error(String.format("Could not auto-clean artifact '%s'.", repoPath.getId()), e);
                }
            } else {
                log.warn("Querying for unused artifacts returned a used one " + repoPath);
            }
        }

        log.info("Auto-clean on the repository '{}' has ended. {} artifact(s) were cleaned",
                repoKey, cleanedArtifactsCount);
    }

    /**
     * Returns the given number of hours, in milliseconds
     *
     * @param hours Number of hours to convert
     * @return Number of hours - In milliseconds
     */
    private long getHoursInMillis(int hours) {
        return TimeUnit.HOURS.toMillis(hours);
    }
}
