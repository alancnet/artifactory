/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.snapshot.SnapshotVersionsRetriever;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.Reloadable;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * The main implementation of the maven unique snapshots clean-up service
 *
 * @author Shay Yaakov
 */
@Service
@Reloadable(beanClass = InternalIntegrationCleanupService.class,
        initAfter = {TaskService.class, InternalRepositoryService.class})
public class IntegrationCleanupServiceImpl implements InternalIntegrationCleanupService {
    private static final Logger log = LoggerFactory.getLogger(IntegrationCleanupServiceImpl.class);

    /**
     * Holds all the snapshot folders to be cleaned up by the job worker (1.0-SNAPSHOT, 1.1-SNAPSHOT etc)
     * Each folder is getting cleaned only if it's time (the time it was putted inside the map) exceeds the global time window
     */
    private ConcurrentMap<IntegrationCleanupCandidate, Long> foldersInTransit = new ConcurrentHashMap<>();
    private Semaphore cleanupSemaphore = new Semaphore(1);
    private final long quietPeriodSecs = ConstantValues.integrationCleanupQuietPeriodSecs.getLong();

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private FolderPruningService pruningService;

    @Autowired
    private TaskService taskService;

    @Override
    public void clean() {
        if (!cleanupSemaphore.tryAcquire()) {
            log.debug("Received integration cleanup request, but a cleanup process is already running.");
            return;
        }

        try {
            if (!foldersInTransit.isEmpty()) {
                for (Map.Entry<IntegrationCleanupCandidate, Long> current : foldersInTransit.entrySet()) {
                    //Make sure to wait the defined amount of time before cleaning
                    log.debug("Checking if integration cleanup can be scheduled for {}",
                            current.getKey().getParentRepoPath());
                    long secondsSinceLastAdded = TimeUnit.MILLISECONDS.toSeconds(
                            System.currentTimeMillis() - current.getValue());

                    if (secondsSinceLastAdded >= quietPeriodSecs) {
                        IntegrationCleanupCandidate queuedCandidate = current.getKey();
                        foldersInTransit.remove(queuedCandidate);
                        conditionalCleanup(queuedCandidate.getFileRepoPath());
                    }
                }
            }
        } finally {
            cleanupSemaphore.release();
        }
    }

    @Override
    public void addItemToCache(RepoPath fileRepoPath) {
        IntegrationCleanupCandidate candidate = new IntegrationCleanupCandidate(fileRepoPath);
        Long previousValue = foldersInTransit.putIfAbsent(candidate, System.currentTimeMillis());
        if (previousValue == null) {
            log.debug("Added integration folder '{}' to folders in transit map.", fileRepoPath.getParent());
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void init() {
        TaskBase integrationCleanupTask = TaskUtils.createRepeatingTask(IntegrationCleanupJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.integrationCleanupIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(ConstantValues.integrationCleanupIntervalSecs.getLong()));
        taskService.startTask(integrationCleanupTask, false);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
    }

    /**
     * The integration cleanup deletes artifacts according to the snapshot and the classifier,
     * unlike the previous approach that was to deletes artifacts according to the snapshot only,
     * See issue RTFACT-6675
     */
    private void conditionalCleanup(RepoPath repoPath) {
        LocalRepo repo = repositoryService.localRepositoryByKey(repoPath.getRepoKey());
        if (repo == null) {
            return;
        }

        SnapshotVersionsRetriever retriever = new SnapshotVersionsRetriever(false);
        ModuleInfo deployedModuleInfo = repositoryService.getItemModuleInfo(repoPath);
        ModuleInfo baseRevisionModule = getBaseRevisionModuleInfo(deployedModuleInfo);
        TreeMultimap<Calendar, ItemInfo> cleanupCandidates = retriever.collectVersionsItems(repo, baseRevisionModule,
                false);
        Map<String,TreeMultimap<Calendar, ItemInfo>> cleanupCandidatesByClassifier=forkByClassifier(cleanupCandidates);
        for (TreeMultimap<Calendar, ItemInfo> calendarItemInfoTreeMultimap : cleanupCandidatesByClassifier.values()) {
            while (calendarItemInfoTreeMultimap.keySet().size() > repo.getMaxUniqueSnapshots()) {
                performCleanup(calendarItemInfoTreeMultimap);
            }
        }

    }



    private Map<String, TreeMultimap<Calendar, ItemInfo>> forkByClassifier(
            TreeMultimap<Calendar, ItemInfo> cleanupCandidates) {
        Map<String, TreeMultimap<Calendar, ItemInfo>> result= Maps.newHashMap();
        for (Calendar calendar : cleanupCandidates.keySet()) {
            NavigableSet<ItemInfo> itemInfos = cleanupCandidates.get(calendar);
            for (ItemInfo itemInfo : itemInfos) {
                String classifier=resolveClassifier(itemInfo);
                TreeMultimap<Calendar, ItemInfo> classifierMap = result.get(classifier);
                if(classifierMap==null){
                    //classifierMap= TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural().reverse());
                    classifierMap= TreeMultimap.create(Ordering.natural(), Ordering.natural());;
                    result.put(classifier,classifierMap);
                }
                classifierMap.put(calendar,itemInfo);
            }
        }
        return result;
    }

    private String resolveClassifier(ItemInfo itemInfo) {
        String classifier = repositoryService.getItemModuleInfo(itemInfo.getRepoPath()).getClassifier();
        return classifier!=null?classifier:"";
    }

    private ModuleInfo getBaseRevisionModuleInfo(ModuleInfo deployedModuleInfo) {
        return new ModuleInfoBuilder().organization(deployedModuleInfo.getOrganization()).
                module(deployedModuleInfo.getModule()).baseRevision(deployedModuleInfo.getBaseRevision()).build();
    }

    private void performCleanup(TreeMultimap<Calendar, ItemInfo> cleanupCandidates) {
        Calendar first = cleanupCandidates.keySet().first();

        Set<RepoPath> parents = Sets.newHashSet();
        SortedSet<ItemInfo> itemsToRemove = cleanupCandidates.removeAll(first);
        for (ItemInfo itemToRemove : itemsToRemove) {
            RepoPath repoPath = itemToRemove.getRepoPath();
            repositoryService.undeploy(repoPath, false, false);
            parents.add(repoPath.getParent());
            log.info("Removed old unique snapshot '{}'.", itemToRemove.getRelPath());
        }
        // May need to prune the parents of deleted files
        for (RepoPath parent : parents) {
            pruningService.prune(parent);
        }
    }
}
