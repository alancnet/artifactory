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

package org.artifactory.storage.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.quota.QuotaConfigDescriptor;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.mbean.MBeanRegistrationService;
import org.artifactory.schedule.BaseTaskServiceDescriptorHandler;
import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.FileStoreStorageSummary;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.storage.binstore.service.BinaryStoreGarbageCollectorJob;
import org.artifactory.storage.binstore.service.InternalBinaryStore;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.fs.repo.RepoStorageSummary;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.mbean.ManagedStorage;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.artifactory.api.repo.storage.RepoStorageSummaryInfo.RepositoryType;

/**
 * @author yoavl
 */
@Service
@Reloadable(beanClass = InternalStorageService.class, initAfter = TaskService.class)
public class StorageServiceImpl implements InternalStorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private DbService dbService;

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private InternalBinaryStore binaryStore;

    @Autowired
    private FileService fileService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    private boolean derbyUsed;

    @Override
    public void compress(BasicStatusHolder statusHolder) {
        if (!derbyUsed) {
            statusHolder.error("Compress command is not supported on current database type.", log);
            return;
        }

        logStorageSizes();
        dbService.compressDerbyDb(statusHolder);
        logStorageSizes();
    }

    @Override
    public void logStorageSizes() {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        File derbyDirectory = new File(artifactoryHome.getDataDir(), "derby");
        long sizeOfDirectory = FileUtils.sizeOfDirectory(derbyDirectory);
        log.info("Derby database storage size: {} ({})", StorageUnit.toReadableString(sizeOfDirectory), derbyDirectory);
    }

    @Override
    public void ping() {
        binaryStore.ping();
    }

    @Override
    public FileStoreStorageSummary getFileStoreStorageSummary() {
        File binariesFolder = binaryStore.getBinariesDir();
        return new FileStoreStorageSummary(binariesFolder, storageProperties);
    }

    @Override
    public StorageQuotaInfo getStorageQuotaInfo(long fileContentLength) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        QuotaConfigDescriptor quotaConfig = descriptor.getQuotaConfig();
        if (quotaConfig == null) {
            return null;
        }
        if (!quotaConfig.isEnabled()) {
            return null;
        }
        File binariesFolder = binaryStore.getBinariesDir();
        if (binariesFolder == null) {
            return null;
        }

        return new StorageQuotaInfo(binariesFolder, quotaConfig.getDiskSpaceLimitPercentage(),
                quotaConfig.getDiskSpaceWarningPercentage(), fileContentLength);
    }

    @Override
    public StorageSummaryInfo getStorageSummaryInfo() {
        Set<RepoStorageSummary> summaries = fileService.getRepositoriesStorageSummary();
        filterGlobalRepoIfNeeded(summaries);
        List<RepoDescriptor> repos = Lists.newArrayList();
        repos.addAll(repositoryService.getLocalAndCachedRepoDescriptors());
        repos.addAll(repositoryService.getVirtualRepoDescriptors());
        final ImmutableMap<String, RepoDescriptor> reposMap =
                Maps.uniqueIndex(repos, new Function<RepoDescriptor, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable RepoDescriptor input) {
                        if (input == null) {
                            return null;
                        }
                        return input.getKey();
                    }
                });
        Iterable<RepoStorageSummaryInfo> infos = Iterables.transform(summaries,
                new Function<RepoStorageSummary, RepoStorageSummaryInfo>() {
                    @Override
                    public RepoStorageSummaryInfo apply(RepoStorageSummary r) {
                        RepositoryType repoType = getRepoType(r.getRepoKey(), reposMap);
                        RepoDescriptor repoDescriptor = reposMap.get(r.getRepoKey());
                        String repoTypeName = "UnKnown";
                        if (repoDescriptor != null) {
                            repoTypeName = repoDescriptor.getType().name();
                        }
                        RepoStorageSummaryInfo repoStorageSummaryInfo = new RepoStorageSummaryInfo(
                                r.getRepoKey(), repoType, r.getFoldersCount(), r.getFilesCount(), r.getUsedSpace(),
                                repoTypeName);
                        return repoStorageSummaryInfo;
                    }

                    private RepositoryType getRepoType(String repoKey,
                            ImmutableMap<String, RepoDescriptor> repoDescriptors) {
                        RepoDescriptor repoDescriptor = repoDescriptors.get(repoKey);
                        if (repoDescriptor == null) {
                            return RepositoryType.BROKEN;
                        } else if (repoDescriptor instanceof RemoteRepoDescriptor) {
                            return RepositoryType.REMOTE;
                        } else if (repoDescriptor instanceof VirtualRepoDescriptor) {
                            return RepositoryType.VIRTUAL;
                        } else if (repoDescriptor instanceof LocalCacheRepoDescriptor) {
                            return RepositoryType.CACHE;
                        } else if (repoDescriptor instanceof LocalRepoDescriptor) {
                            return RepositoryType.LOCAL;
                        } else {
                            return RepositoryType.NA;
                        }
                    }
                }
        );

        BinariesInfo binariesInfo = binaryStore.getBinariesInfo();

        return new StorageSummaryInfo(Sets.newHashSet(infos), binariesInfo);
    }

    private void filterGlobalRepoIfNeeded(Set<RepoStorageSummary> summaries) {
        if (ConstantValues.disableGlobalRepoAccess.getBoolean()) {
            Iterables.removeIf(summaries, new Predicate<RepoStorageSummary>() {
                @Override
                public boolean apply(RepoStorageSummary summary) {
                    return VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY.equals(summary.getRepoKey());
                }
            });
        }
    }

    @Override
    public void callManualGarbageCollect(BasicStatusHolder statusHolder) {
        taskService.checkCanStartManualTask(BinaryStoreGarbageCollectorJob.class, statusHolder);
        if (!statusHolder.isError()) {
            try {
                execOneGcAndWait(true);
            } catch (Exception e) {
                statusHolder.error("Error activating Artifactory Storage Garbage Collector: " + e.getMessage(), e,
                        log);
            }
        }
    }

    @Override
    public void pruneUnreferencedFileInDataStore(BasicStatusHolder statusHolder) {
        binaryStore.prune(statusHolder);
    }

    private String execOneGcAndWait(boolean waitForCompletion) {
        TaskBase task = TaskUtils.createManualTask(BinaryStoreGarbageCollectorJob.class, 0L);
        String token = taskService.startTask(task, true, true);
        if (waitForCompletion) {
            taskService.waitForTaskCompletion(token);
        }
        return token;
    }

    @Override
    public boolean isDerbyUsed() {
        return derbyUsed;
    }

    @Override
    public void init() {
        derbyUsed = dbService.getDatabaseType() == DbType.DERBY;

        ContextHelper.get().beanForType(MBeanRegistrationService.class).
                register(new ManagedStorage(binaryStore), "Storage", "Binary Storage");

        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        new GcSchedulerHandler(descriptor.getGcConfig(), null).reschedule();
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        CentralConfigDescriptor descriptor = centralConfigService.getDescriptor();
        new GcSchedulerHandler(descriptor.getGcConfig(), oldDescriptor.getGcConfig()).reschedule();
    }

    @Override
    public void destroy() {
        new GcSchedulerHandler(null, null).unschedule();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        //nop
    }

    static class GcSchedulerHandler extends BaseTaskServiceDescriptorHandler<GcConfigDescriptor> {

        final List<GcConfigDescriptor> oldDescriptorHolder = Lists.newArrayList();
        final List<GcConfigDescriptor> newDescriptorHolder = Lists.newArrayList();

        GcSchedulerHandler(GcConfigDescriptor newDesc, GcConfigDescriptor oldDesc) {
            if (newDesc != null) {
                newDescriptorHolder.add(newDesc);
            }
            if (oldDesc != null) {
                oldDescriptorHolder.add(oldDesc);
            }
        }

        @Override
        public String jobName() {
            return "Garbage Collector";
        }

        @Override
        public List<GcConfigDescriptor> getNewDescriptors() {
            return newDescriptorHolder;
        }

        @Override
        public List<GcConfigDescriptor> getOldDescriptors() {
            return oldDescriptorHolder;
        }

        @Override
        public Predicate<Task> getAllPredicate() {
            return new Predicate<Task>() {
                @Override
                public boolean apply(@Nullable Task input) {
                    return (input != null) && BinaryStoreGarbageCollectorJob.class.isAssignableFrom(input.getType());
                }
            };
        }

        @Override
        public Predicate<Task> getPredicate(@Nonnull GcConfigDescriptor descriptor) {
            return getAllPredicate();
        }

        @Override
        public void activate(@Nonnull GcConfigDescriptor descriptor, boolean manual) {
            AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
            CoreAddons coreAddons = addonsManager.addonByType(CoreAddons.class);
            TaskBase garbageCollectorTask;
            if (coreAddons.isAol()) {
                garbageCollectorTask = TaskUtils.createRepeatingTask(BinaryStoreGarbageCollectorJob.class,
                        TimeUnit.SECONDS.toMillis(ConstantValues.gcIntervalSecs.getLong()),
                        TimeUnit.SECONDS.toMillis(ConstantValues.gcDelaySecs.getLong()));
            } else {
                garbageCollectorTask = TaskUtils.createCronTask(BinaryStoreGarbageCollectorJob.class,
                        descriptor.getCronExp());
            }
            InternalContextHelper.get().getTaskService().startTask(garbageCollectorTask, manual);
        }

        @Override
        public GcConfigDescriptor findOldFromNew(@Nonnull GcConfigDescriptor newDescriptor) {
            return oldDescriptorHolder.isEmpty() ? null : oldDescriptorHolder.get(0);
        }
    }
}