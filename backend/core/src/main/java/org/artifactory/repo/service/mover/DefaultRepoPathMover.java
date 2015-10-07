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
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.StatusEntry;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The ordinary repo-path move/copy implementation
 *
 * @author Noam Y. Tenne
 */
class DefaultRepoPathMover extends BaseRepoPathMover {

    private static final Logger log = LoggerFactory.getLogger(DefaultRepoPathMover.class);

    private final ReplicationAddon replicationAddon;

    DefaultRepoPathMover(MoveMultiStatusHolder status, MoverConfig moverConfig) {
        super(status, moverConfig);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
    }

    MoveMultiStatusHolder moveOrCopy(VfsItem sourceItem, RepoRepoPath<LocalRepo> targetRrp) {

        // See org.artifactory.repo.service.mover.MoverConfig,
        // copy(Set<RepoPath> pathsToCopy, String targetLocalRepoKey, ...)
        // move(Set<RepoPath> pathsToCopy, String targetLocalRepoKey, ...)
        if (unixStyleBehavior) {
            // if the target is a directory and it exists we move/copy the source UNDER the target directory (ie, we
            // don't replace it - this is the default unix filesystem behavior).
            VfsItem targetFsItem = targetRrp.getRepo().getMutableFsItem(targetRrp.getRepoPath());
            if (targetFsItem != null && targetFsItem.isFolder()) {
                String adjustedPath = targetRrp.getRepoPath().getPath() + "/" + sourceItem.getName();
                targetRrp = new RepoRepoPath<>(targetRrp.getRepo(),
                        InternalRepoPathFactory.create(targetRrp.getRepoPath().getRepoKey(), adjustedPath));
            }
        }
        // ok start moving
        moveCopyRecursive(sourceItem, targetRrp);

        // recalculate maven metadata on affected repositories
        if (!dryRun) {
            clearEmptySourceDirs(sourceItem);
            RepoPath folderRepoPath = getFolderRepoPath(sourceItem);
            if (folderRepoPath != null) {
                calculateMavenMetadata(targetRrp, folderRepoPath);
            }
        }
        return status;
    }

    private void moveCopyRecursive(VfsItem source, RepoRepoPath<LocalRepo> targetRrp) {

        if (errorsOrWarningsOccurredAndFailFast()) {
            return;
        }
        if (source.isFolder()) {
            handleDir((VfsFolder) source, targetRrp);
        } else {
            // the source is a file
            handleFile(source, targetRrp);
        }
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    private void handleDir(VfsFolder source, RepoRepoPath<LocalRepo> targetRrp) {
        MutableVfsFolder targetFolder = null;
        RepoPath targetRepoPath = targetRrp.getRepoPath();
        if (canMove(source, targetRrp)) {
            if (!dryRun) {
                StatusEntry lastError = status.getLastError();
                if (copy) {
                    storageInterceptors.beforeCopy(source, targetRepoPath, status, properties);
                } else {
                    storageInterceptors.beforeMove(source, targetRepoPath, status, properties);
                }
                if (status.getCancelException(lastError) != null) {
                    return;
                }
                targetFolder = shallowCopyDirectory(source, targetRrp);
            }
        } else if (!contains(targetRrp) ||
                targetRrp.getRepo().getImmutableFsItem(targetRrp.getRepoPath()).isFile()) {
            // target repo doesn't accept this path and it doesn't already contain it OR the target is a file
            // so there is no point to continue to the children
            status.error("Cannot create/override the path '" + targetRrp.getRepoPath() + "'. " +
                    "Skipping this path and all its children.", log);
            return;
        }
        List<VfsItem> children = source.getImmutableChildren();
        RepoPath originalRepoPath = targetRrp.getRepoPath();
        for (VfsItem child : children) {
            // update the cached object with the child's repo path.
            targetRrp = new RepoRepoPath<>(targetRrp.getRepo(),
                    InternalRepoPathFactory.create(originalRepoPath, child.getName()));
            // recursive call with the child
            moveCopyRecursive(child, targetRrp);
        }
        saveSession();  // save the session before checking if the folder is empty
        String path = targetRepoPath.getPath();
        DbLocalRepo targetRepo = (DbLocalRepo) targetRrp.getRepo();
        if (shouldRemoveSourceFolder(source)) {
            storageInterceptors.afterMove(source, targetFolder, status, properties);
            deleteAndReplicateEvent(source);
        } else if (!dryRun && copy && targetRepo.isPathPatternValid(targetRepoPath, path)) {
            storageInterceptors.afterCopy(source, targetFolder, status, properties);
        }
        if (shouldRemoveTargetFolder(targetFolder, children.size())) {
            deleteAndReplicateEvent(targetFolder); // target folder is empty remove it immediately
        }
    }

    /**
     * This method copies the source folder to the target folder including the folder metadata, excluding children
     *
     * @param sourceFolder src
     * @param targetRrp    dest
     * @return the new folder dest created
     */
    private MutableVfsFolder shallowCopyDirectory(VfsFolder sourceFolder, RepoRepoPath<LocalRepo> targetRrp) {
        assertNotDryRun();
        LocalRepo targetRepo = targetRrp.getRepo();
        RepoPath targetRepoPath = InternalRepoPathFactory.create(targetRepo.getKey(),
                targetRrp.getRepoPath().getPath());

        MutableVfsFolder targetFolder = targetRepo.getMutableFolder(targetRepoPath);
        if (targetFolder == null) {
            log.debug("Creating target folder {}", targetRepoPath);
            targetFolder = targetRepo.createOrGetFolder(targetRepoPath);
        } else {
            log.debug("Target folder {} already exist", targetRepoPath);
        }
        status.folderMoved();
        targetFolder.fillInfo(sourceFolder.getInfo());

        // copy relevant metadata from source to target
        log.debug("Copying folder metadata to {}", targetRepoPath);
        targetFolder.setProperties(sourceFolder.getProperties());
        replicationAddon.offerLocalReplicationPropertiesChangeEvent(targetRepoPath);

        return targetFolder;
    }
}