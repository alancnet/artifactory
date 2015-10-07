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

package org.artifactory.maven.index;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.lucene.store.FSDirectory;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.io.TempFileStreamHandle;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.StoringRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.schedule.BaseTaskServiceDescriptorHandler;
import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.Files;
import org.artifactory.util.Pair;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yoav Landman
 */
@Service
@Reloadable(beanClass = InternalMavenIndexerService.class,
        initAfter = {TaskService.class, InternalRepositoryService.class})
public class MavenIndexerServiceImpl implements InternalMavenIndexerService {
    private static final Logger log = LoggerFactory.getLogger(MavenIndexerServiceImpl.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private CentralConfigService centralConfig;

    @Autowired
    private InternalRepositoryService repositoryService;

    @Override
    public void init() {
        new IndexerSchedulerHandler(getDescriptor(), null).reschedule();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        new IndexerSchedulerHandler(getDescriptor(), oldDescriptor.getIndexer()).reschedule();
    }

    private IndexerDescriptor getDescriptor() {
        return centralConfig.getDescriptor().getIndexer();
    }

    @Override
    public void scheduleImmediateIndexing(MutableStatusHolder statusHolder) {
        scheduleIndexer(statusHolder, new MavenIndexerRunSettings(true, false, null));
    }

    @Override
    public void runSpecificIndexer(MutableStatusHolder statusHolder, List<String> repoKeys,
            boolean forceRemoteDownload) {
        scheduleIndexer(statusHolder, new MavenIndexerRunSettings(true, forceRemoteDownload, repoKeys));
    }

    private void scheduleIndexer(MutableStatusHolder statusHolder, MavenIndexerRunSettings settings) {
        taskService.checkCanStartManualTask(MavenIndexerJob.class, statusHolder);
        if (!statusHolder.isError()) {
            try {
                StringBuilder logMessageBuilder = new StringBuilder("Activating indexer ");
                List<String> repoKeys = settings.getRepoKeys();
                if ((repoKeys != null) && !repoKeys.isEmpty()) {
                    logMessageBuilder.append("for repo '").append(Arrays.toString(repoKeys.toArray())).append("' ");
                }
                logMessageBuilder.append("manually");
                log.info(logMessageBuilder.toString());
                TaskBase task = TaskUtils.createManualTask(MavenIndexerJob.class, 0L);
                task.addAttribute(MavenIndexerJob.SETTINGS, settings);
                taskService.startTask(task, true, true);
            } catch (Exception e) {
                log.error("Error scheduling the indexer.", e);
            }
        }
    }

    @Override
    public void destroy() {
        new IndexerSchedulerHandler(null, null).unschedule();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void index(MavenIndexerRunSettings settings) {
        log.info("Starting Maven indexing");
        IndexerDescriptor descriptor = getDescriptor();
        if (!settings.isForceRemoteDownload() && !descriptor.isEnabled() && !settings.isManualRun()) {
            log.debug("Indexer is disabled - doing nothing.");
            return;
        }

        Set<? extends RepoDescriptor> includedRepositories;
        List<String> repoKeys = settings.getRepoKeys();
        if ((repoKeys == null) || repoKeys.isEmpty()) {
            includedRepositories = descriptor.getIncludedRepositories();
            if (includedRepositories == null) {
                // Nothing to index
                log.info("Indexer activated but has no repository declared to index - doing nothing.");
                return;
            }
        } else {
            // only calculate the requested repositories
            includedRepositories = calcSpecificReposForIndexing(repoKeys);
        }

        log.info("Starting non virtual repositories indexing...");
        List<RealRepo> indexedRepos = getNonVirtualRepositoriesToIndex(includedRepositories);
        log.info("Non virtual repositories to index: {}", indexedRepos);
        //Do the indexing work
        for (RealRepo indexedRepo : indexedRepos) {
            //Check if we need to stop/suspend
            if (taskService.pauseOrBreak()) {
                log.info("Stopped indexing on demand");
                return;
            }
            MavenIndexManager mavenIndexManager = new MavenIndexManager(indexedRepo);
            try {
                boolean remoteIndexExists = mavenIndexManager.fetchRemoteIndex(settings.isForceRemoteDownload());
                mavenIndexManager.createLocalIndex(settings.getFireTime(), remoteIndexExists);

                //Check again if we need to stop/suspend
                if (taskService.pauseOrBreak()) {
                    log.info("Stopped indexing on demand");
                    return;
                }
                mavenIndexManager.saveIndexFiles();
            } catch (Exception e) {
                //If we failed to index because of a socket timeout, issue a terse warning instead of a complete stack
                //trace
                Throwable cause = ExceptionUtils.getCauseOfTypes(e, SocketTimeoutException.class);
                if (cause != null) {
                    log.warn("Indexing for repo '" + indexedRepo.getKey() + "' failed: " + e.getMessage() + ".");
                } else {
                    //Just report - don't stop indexing of other repos
                    log.error("Indexing for repo '" + indexedRepo.getKey() + "' failed.", e);
                }
            }
        }
        mergeVirtualRepoIndexes(includedRepositories, indexedRepos);
        log.info("Finished Maven indexing...");
    }

    private Set<? extends RepoDescriptor> calcSpecificReposForIndexing(List<String> repoKeys) {
        Set<RepoBaseDescriptor> repos = Sets.newHashSet();
        repos.addAll(repositoryService.getLocalRepoDescriptors());
        repos.addAll(repositoryService.getRemoteRepoDescriptors());
        repos.addAll(getAllVirtualReposExceptGlobal());
        repos.removeIf(descriptor -> !repoKeys.contains(descriptor.getKey()));
        return repos;
    }

    private List<RealRepo> getNonVirtualRepositoriesToIndex(
            @Nullable Set<? extends RepoDescriptor> includedRepositories) {
        List<RealRepo> indexedRepos = repositoryService.getLocalAndRemoteRepositories();
        if (includedRepositories != null) {
            indexedRepos.removeIf(realRepo -> !includedRepositories.contains(realRepo.getDescriptor()));
        }
        return indexedRepos;
    }

    public void mergeVirtualRepoIndexes(@Nonnull Set<? extends RepoDescriptor> includedRepositories,
            List<RealRepo> indexedRepos) {
        List<VirtualRepo> virtualRepos = getVirtualRepos(includedRepositories);
        log.info("Virtual repositories to index: {}", virtualRepos);
        //Keep a list of extracted index dirs for all the local repo indexes for merging
        Map<StoringRepo, FSDirectory> extractedLocalRepoIndexes = new HashMap<>();
        try {
            //Merge virtual repo indexes
            for (VirtualRepo virtualRepo : virtualRepos) {
                //Check if we need to stop/suspend
                if (taskService.pauseOrBreak()) {
                    log.info("Stopped indexing on demand");
                    return;
                }
                Set<LocalRepo> localRepos = new HashSet<>();
                localRepos.addAll(virtualRepo.getResolvedLocalRepos());
                localRepos.addAll(virtualRepo.getResolvedLocalCachedRepos());
                //Create a temp lucene dir and merge each local into it
                ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
                File dir = Files.createRandomDir(artifactoryHome.getTempWorkDir(), "artifactory.merged-index");
                RepoIndexer indexer = null;
                try {
                    indexer = new RepoIndexer(virtualRepo);
                    indexer.createContext(dir);

                    log.info("Creating virtual repository index '{}'", virtualRepo);
                    //Take the local index from each relevant indexed repo (for remote take local cache)
                    for (RealRepo indexedRepo : indexedRepos) {
                        //Check if we need to stop/suspend
                        if (taskService.pauseOrBreak()) {
                            log.info("Stopped indexing on demand");
                            return;
                        }
                        LocalRepo localRepo = indexedRepo.isLocal() ? (LocalRepo) indexedRepo :
                                ((RemoteRepo) indexedRepo).getLocalCacheRepo();
                        try {
                            //Extract aside the index from the local repo
                            if (localRepos.contains(localRepo)) {
                                log.debug("Merging index of '{}' to index of virtual repo '{}'", localRepo,
                                        virtualRepo);
                                indexer.mergeInto(localRepo, extractedLocalRepoIndexes);
                            }
                        } catch (Exception e) {
                            log.warn("Could not merge index of local repo '{}' into virtual repo '{}'",
                                    localRepo.getKey(), virtualRepo.getKey());
                        }
                    }
                    //Store the index into the virtual repo
                    //Get the last gz and props and store them - we need to return them or create them from the dir
                    Pair<TempFileStreamHandle, TempFileStreamHandle> tempFileStreamHandlesPair =
                            indexer.createIndex(dir, false);
                    ResourceStreamHandle indexHandle = tempFileStreamHandlesPair.getFirst();
                    ResourceStreamHandle properties = tempFileStreamHandlesPair.getSecond();
                    MavenIndexManager mavenIndexManager =
                            new MavenIndexManager(indexer.getRepo(), indexHandle, properties);
                    mavenIndexManager.saveIndexFiles();
                } finally {
                    if (indexer != null) {
                        indexer.removeTempIndexFiles(dir);
                    }
                    org.apache.commons.io.FileUtils.deleteQuietly(dir);
                }
            }
        } catch (Exception e) {
            log.error("Could not merge virtual repository indexes.", e);
        } finally {
            //Delete temp extracted dirs
            for (FSDirectory directory : extractedLocalRepoIndexes.values()) {
                org.apache.commons.io.FileUtils.deleteQuietly(directory.getFile());
            }
        }
    }

    /**
     * Returns a filtered list of virtual repositories based on the excluded repository list
     *
     * @param includedRepositories List of repositories to index
     * @return List of virtual repositories to index
     */
    private List<VirtualRepo> getVirtualRepos(@Nonnull Set<? extends RepoDescriptor> includedRepositories) {
        List<VirtualRepo> virtualRepositories = repositoryService.getVirtualRepositories();
        List<VirtualRepo> virtualRepositoriesCopy = new ArrayList<>(virtualRepositories);
        virtualRepositoriesCopy.removeIf(virtualRepo -> {
            boolean isFilterGlobalRepo = VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY.equals(virtualRepo.getKey())
                    && ConstantValues.disableGlobalRepoAccess.getBoolean();

            boolean isIncluded = includedRepositories
                    .stream()
                    .map(RepoDescriptor::getKey)
                    .collect(Collectors.toList())
                    .contains(virtualRepo.getKey());

            return isFilterGlobalRepo || !isIncluded;
        });

        return virtualRepositoriesCopy;
    }

    /**
     * Returns the complete list of virtual repository descriptors, apart from the global one (repo)
     *
     * @return List of all virtual repository descriptors apart from the global one
     */
    private List<VirtualRepoDescriptor> getAllVirtualReposExceptGlobal() {
        List<VirtualRepoDescriptor> virtualRepositoriesCopy =
                new ArrayList<>(repositoryService.getVirtualRepoDescriptors());
        VirtualRepoDescriptor dummyGlobal = new VirtualRepoDescriptor();
        dummyGlobal.setKey(VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY);
        virtualRepositoriesCopy.remove(dummyGlobal);
        return virtualRepositoriesCopy;
    }

    static class IndexerSchedulerHandler extends BaseTaskServiceDescriptorHandler<IndexerDescriptor> {
        final List<IndexerDescriptor> oldDescriptorHolder = Lists.newArrayList();
        final List<IndexerDescriptor> newDescriptorHolder = Lists.newArrayList();

        IndexerSchedulerHandler(IndexerDescriptor newDesc, IndexerDescriptor oldDesc) {
            if (newDesc != null) {
                newDescriptorHolder.add(newDesc);
            }
            if (oldDesc != null) {
                oldDescriptorHolder.add(oldDesc);
            }
        }

        @Override
        public String jobName() {
            return "Indexer";
        }

        @Override
        public List<IndexerDescriptor> getNewDescriptors() {
            return newDescriptorHolder;
        }

        @Override
        public List<IndexerDescriptor> getOldDescriptors() {
            return oldDescriptorHolder;
        }

        @Override
        public Predicate<Task> getAllPredicate() {
            return new Predicate<Task>() {
                @Override
                public boolean apply(Task input) {
                    return MavenIndexerJob.class.isAssignableFrom(input.getType());
                }
            };
        }

        @Override
        public Predicate<Task> getPredicate(@Nonnull IndexerDescriptor descriptor) {
            return getAllPredicate();
        }

        @Override
        public void activate(@Nonnull IndexerDescriptor descriptor, boolean manual) {
            String cronExp = descriptor.getCronExp();
            if (descriptor.isEnabled() && cronExp != null) {
                TaskBase task = TaskUtils.createCronTask(MavenIndexerJob.class, cronExp);
                // Passing null for repo keys because they are taken from the indexer descriptor
                MavenIndexerRunSettings settings = new MavenIndexerRunSettings(false, false, null);
                task.addAttribute(MavenIndexerJob.SETTINGS, settings);
                InternalContextHelper.get().getBean(TaskService.class).startTask(task, false, manual);
                log.info("Indexer activated with cron expression '{}'.", cronExp);
            } else {
                log.debug("No indexer cron expression is configured. Indexer will be disabled.");
            }
        }

        @Override
        public IndexerDescriptor findOldFromNew(@Nonnull IndexerDescriptor newDescriptor) {
            return oldDescriptorHolder.isEmpty() ? null : oldDescriptorHolder.get(0);
        }
    }
}