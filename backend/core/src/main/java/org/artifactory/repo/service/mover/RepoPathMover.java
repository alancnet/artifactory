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

package org.artifactory.repo.service.mover;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.Request;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.fs.VfsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Moves a folder repo path from one local repository to another non-cache local repository. Only files and folders that
 * are accepted by the target repository will be moved. This class is stateless.
 *
 * @author Yossi Shaul
 */
@Component
public class RepoPathMover {
    private static final Logger log = LoggerFactory.getLogger(RepoPathMover.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private InternalRepositoryService repositoryService;

    @Request(aggregateEventsByTimeWindow = true)
    public void moveOrCopy(MoveMultiStatusHolder status, MoverConfig moverConfig) {
        boolean isDryRun = moverConfig.isDryRun();
        RepoPath fromRepoPath = moverConfig.getFromRepoPath();

        // don't output to the logger if executing in dry run
        status.setActivateLogging(!isDryRun);

        LocalRepo sourceRepo = getLocalRepo(fromRepoPath.getRepoKey());
        VfsItem sourceItem = moverConfig.isCopy() ? sourceRepo.getImmutableFsItem(fromRepoPath) :
                sourceRepo.getMutableFsItem(fromRepoPath);
        if (sourceItem == null) {
            throw new IllegalArgumentException("Could not find item at " + fromRepoPath);
        }

        RepoPath targetLocalRepoPath = moverConfig.getTargetLocalRepoPath();
        String opType = (moverConfig.isCopy()) ? "copy" : "move";
        if (fromRepoPath.equals(targetLocalRepoPath)) {
            status.error(String.format("Skipping %s %s: Destination and source are the same", opType, fromRepoPath),
                    log);
            return;
        }

        RepoRepoPath<LocalRepo> targetRrp = repositoryService.getRepoRepoPath(targetLocalRepoPath);
        if (targetRrp.getRepo().isCache()) {
            throw new IllegalArgumentException(String.format("Target repository %s is a cache repository. %s to cache" +
                    " repositories is not allowed.", targetLocalRepoPath.getRepoKey(), opType));
        }

        LayoutsCoreAddon layoutsCoreAddon = addonsManager.addonByType(LayoutsCoreAddon.class);
        boolean canCrossLayouts = !moverConfig.isSuppressLayouts() &&
                layoutsCoreAddon.canCrossLayouts(sourceRepo.getDescriptor().getRepoLayout(),
                        targetRrp.getRepo().getDescriptor().getRepoLayout());

        if (canCrossLayouts) {
            layoutsCoreAddon.performCrossLayoutMoveOrCopy(status, moverConfig,
                    sourceRepo, targetRrp.getRepo(), sourceItem);
        } else {
            new DefaultRepoPathMover(status, moverConfig).moveOrCopy(sourceItem, targetRrp);
        }
    }

    private LocalRepo getLocalRepo(String repoKey) {
        LocalRepo targetLocalRepo = repositoryService.localOrCachedRepositoryByKey(repoKey);
        if (targetLocalRepo == null) {
            throw new IllegalArgumentException("Local repository " + repoKey +
                    " not found (repository is not local or doesn't exist)");
        }
        return targetLocalRepo;
    }
}
