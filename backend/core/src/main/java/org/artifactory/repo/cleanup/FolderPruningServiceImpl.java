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

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.schedule.TaskBase;
import org.artifactory.schedule.TaskService;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.Reloadable;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous implementation of folder pruning.
 * This implementation allows only one thread to perform actual pruning.
 *
 * @author Shay Yaakov
 */
@Service
@Reloadable(beanClass = InternalFolderPruningService.class,
        initAfter = {TaskService.class, InternalRepositoryService.class})
public class FolderPruningServiceImpl implements InternalFolderPruningService {
    private static final Logger log = LoggerFactory.getLogger(FolderPruningServiceImpl.class);

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private TaskService taskService;

    private ConcurrentLinkedQueue<CandidatePruningFolder> foldersToPrune = new ConcurrentLinkedQueue<>();
    private Semaphore pruneSemaphore = new Semaphore(1);

    static class CandidatePruningFolder {
        final RepoPath folderPath;
        final Long insertTime;

        CandidatePruningFolder(RepoPath folderPath, Long insertTime) {
            this.folderPath = folderPath;
            this.insertTime = insertTime;
        }
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public void init() {
        TaskBase folderPruningTask = TaskUtils.createRepeatingTask(FolderPruningJob.class,
                TimeUnit.SECONDS.toMillis(ConstantValues.folderPruningIntervalSecs.getLong()),
                TimeUnit.SECONDS.toMillis(ConstantValues.folderPruningIntervalSecs.getLong()));
        taskService.startTask(folderPruningTask, false);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doPrune() {
        if (!pruneSemaphore.tryAcquire()) {
            log.debug("Received prune empty folders request, but a pruning process is already running.");
            return;
        }

        try {
            CandidatePruningFolder queuedFolderRepoPath;
            while ((queuedFolderRepoPath = foldersToPrune.peek()) != null) {
                long timeSinceRequest = System.currentTimeMillis() - queuedFolderRepoPath.insertTime;
                long quietPeriod = TimeUnit.SECONDS.toMillis(ConstantValues.folderPruningQuietPeriodSecs.getLong());
                long delta = quietPeriod - timeSinceRequest;
                if (delta > 0) {
                    // In the quiet period let's wait for better time
                    if (delta < quietPeriod / 2L) {
                        // Let's just wait here
                        Thread.sleep(delta);
                        continue;
                    }
                    // Wait for next execution
                    break;
                }
                queuedFolderRepoPath = foldersToPrune.poll();
                if (queuedFolderRepoPath == null) {
                    break;
                }
                doPruneSingleRepoPath(queuedFolderRepoPath.folderPath);
            }
        } catch (InterruptedException e) {
            log.error("Got interrupted waiting for quiet period", e);
        } finally {
            pruneSemaphore.release();
        }
    }

    @Override
    public void prune(RepoPath folderRepoPath) {
        log.trace("Prune called for {}", folderRepoPath);
        if (folderRepoPath.isRoot()) {
            return;
        }

        if (foldersToPrune.offer(new CandidatePruningFolder(folderRepoPath, System.currentTimeMillis()))) {
            log.debug("Added folder repo path '{}' to pruning queue.", folderRepoPath);
        }
    }

    private void doPruneSingleRepoPath(RepoPath repoPathToPrune) {
        try {
            ItemInfo unlockedItemInfo = repoService.getItemInfo(repoPathToPrune);
            if (!unlockedItemInfo.isFolder()) {
                log.warn("Cannot prune the file '{}', can only prune folders.", repoPathToPrune);
                return;
            }

            List<String> childrenNames = repoService.getChildrenNames(repoPathToPrune);
            if (childrenNames.isEmpty() || hasOnlyMavenMetadataChild(childrenNames)) {
                log.debug("Pruning empty folder '{}'", repoPathToPrune);
                repoService.undeploy(repoPathToPrune, false, true);
            } else {
                log.debug("Skipping pruning of folder '{}' since it has children.", repoPathToPrune);
            }
        } catch (ItemNotFoundRuntimeException e) {
            log.debug("Could not find repo path {}: {}", repoPathToPrune, e.getMessage());
        }
    }

    private boolean hasOnlyMavenMetadataChild(List<String> childrenNames) {
        return childrenNames.size() == 1 && MavenNaming.MAVEN_METADATA_NAME.equals(childrenNames.get(0));
    }
}
