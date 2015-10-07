/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.sapi.search.VfsComparatorType;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryResultType;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.sapi.search.VfsQueryService;
import org.artifactory.schedule.BaseTaskServiceDescriptorHandler;
import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.version.CompoundVersionDetails;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Yoav Luft
 */
@Service
@Reloadable(beanClass = InternalVirtualCacheCleanupService.class,
            initAfter = {TaskService.class, InternalRepositoryService.class})
public class VirtualCacheCleanupServiceImpl implements InternalVirtualCacheCleanupService {

    private static final Logger log = LoggerFactory.getLogger(VirtualCacheCleanupServiceImpl.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private VfsQueryService vfsQueryService;

    @Override
    public void init() {
        reload(null);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        CleanupConfigDescriptor oldCleanupConfig = null;
        if (oldDescriptor != null) {
            oldCleanupConfig = oldDescriptor.getVirtualCacheCleanupConfig();
        }
        CleanupConfigDescriptor virtualCacheCleanupConfig = descriptor.getVirtualCacheCleanupConfig();
        new VirtualCacheCleanupConfigHandler(virtualCacheCleanupConfig, oldCleanupConfig).reschedule();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }

    @Nullable
    @Override
    public String callVirtualCacheCleanup(BasicStatusHolder statusHolder) {
        taskService.checkCanStartManualTask(VirtualCacheCleanupJob.class, statusHolder);
        log.info("Virtual repositories cleanup was scheduled to run.");
        if (!statusHolder.isError()) {
            try {
                TaskBase task = TaskUtils.createManualTask(VirtualCacheCleanupJob.class, 0L);
                return taskService.startTask(task, true, true);
            } catch (Exception e) {
                statusHolder.error("Failed to run virtual cache cleanup", e, log);
            }
        }
        return null;
    }

    @Override
    public void clean() {
        long totalRemovedFiles = 0;
        int maxAgeMinutes = ConstantValues.virtualCleanupMaxAgeHours.getInt();
        if (maxAgeMinutes < 0) {
            log.debug("Cleanup of virtual caches is disabled");
            return;
        }
        log.info("Starting cleanup of virtual repositories");
        for (VirtualRepo virtualRepo : repositoryService.getVirtualRepositories()) {
            totalRemovedFiles += cleanRepo(virtualRepo);
        }
        log.info("Completed virtual repositories cleanup: removed {} cached files.", totalRemovedFiles);
    }

    private long cleanRepo(final VirtualRepo repo) {
        int maxAgeMinutes = ConstantValues.virtualCleanupMaxAgeHours.getInt();
        long expiryTime = new DateTime().minusMinutes(maxAgeMinutes).getMillis();
        VfsQuery query = vfsQueryService.createQuery().expectedResult(VfsQueryResultType.FILE)
                .name(ConstantValues.virtualCleanupNamePattern.getString()).comp(VfsComparatorType.CONTAINS)
                .setSingleRepoKey(repo.getKey())
                .prop("created").comp(VfsComparatorType.LOWER_THAN_EQUAL).val(expiryTime);
        VfsQueryResult queryResult = query.execute(Integer.MAX_VALUE);
        if (queryResult.getCount() > 0) {
            log.info("Found {} cached files in {}", queryResult.getCount(), repo.getKey());
        }
        for (VfsQueryRow result : queryResult.getAllRows()) {
            log.trace("Undeploying old cached file {}", result.getItem().getName());
            repositoryService.undeploy(result.getItem().getRepoPath());
        }
        return queryResult.getCount();
    }

    static class VirtualCacheCleanupConfigHandler
            extends BaseTaskServiceDescriptorHandler<CleanupConfigDescriptor> {

        final List<CleanupConfigDescriptor> oldDescriptorHolder = Lists.newArrayList();
        final List<CleanupConfigDescriptor> newDescriptorHolder = Lists.newArrayList();

        VirtualCacheCleanupConfigHandler(CleanupConfigDescriptor newDesc, CleanupConfigDescriptor oldDesc) {
            if (newDesc != null) {
                newDescriptorHolder.add(newDesc);
            }
            if (oldDesc != null) {
                oldDescriptorHolder.add(oldDesc);
            }
        }

        @Override
        public String jobName() {
            return "Virtual Cache Cleanup";
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
                    return input == null || VirtualCacheCleanupJob.class.isAssignableFrom(input.getType());
                }
            };
        }

        @Override
        public Predicate<Task> getPredicate(@Nonnull CleanupConfigDescriptor descriptor) {
            return getAllPredicate();
        }

        @Override
        public void activate(@Nonnull CleanupConfigDescriptor descriptor, boolean manual) {
            TaskBase cleanupTask = TaskUtils.createCronTask(VirtualCacheCleanupJob.class, descriptor.getCronExp());
            InternalContextHelper.get().getTaskService().startTask(cleanupTask, manual, manual);
        }

        @Override
        public CleanupConfigDescriptor findOldFromNew(@Nonnull CleanupConfigDescriptor newDescriptor) {
            return oldDescriptorHolder.isEmpty() ? null : oldDescriptorHolder.get(0);
        }
    }
}
