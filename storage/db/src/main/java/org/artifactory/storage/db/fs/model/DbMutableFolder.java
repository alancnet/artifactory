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

package org.artifactory.storage.db.fs.model;

import org.artifactory.exception.CancelException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.MutableFolderInfo;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A mutable DB folder.
 *
 * @author Yossi Shaul
 */
public class DbMutableFolder extends DbMutableItem<MutableFolderInfo> implements MutableVfsFolder {
    private static final Logger log = LoggerFactory.getLogger(DbMutableFolder.class);

    public DbMutableFolder(StoringRepo repo, long id, FolderInfo folderInfo) {
        super(repo, id, InfoFactoryHolder.get().copyFolderInfo(folderInfo));
    }

    @Override
    public void fillInfo(FolderInfo source) {
        super.fillInfo(source);
    }

    @Override
    public List<MutableVfsItem> getMutableChildren() {
        return getRepo().getMutableChildren(this);
    }

    @Override
    public List<VfsItem> getImmutableChildren() {
        return getRepo().getImmutableChildren(this);
    }

    @Override
    public boolean hasChildren() {
        return getRepo().hasChildren(this);
    }

    @Override
    public boolean delete() {
        if (isNew()) {
            //TODO: [by YS] until verified
            throw new IllegalStateException("Cannot delete a newly created item: " + getRepoPath());
        }

        if (markForDeletion) {
            return true;
        }

        try {
            fireBeforeDeleteEvent();
        } catch (CancelException e) {
            log.info("Deletion of {} was canceled by user plugin", getRepoPath());
            throw e;
        }

        List<MutableVfsItem> children = getMutableChildren();
        for (MutableVfsItem child : children) {
            child.delete();
        }

        if (!getRepoPath().isRoot()) {
            markForDeletion = true;
        }

        fireAfterDeleteEvent();
        return true;
    }

    @Override
    public void deleteIncludingRoot() {
        if (!getRepoPath().isRoot()) {
            throw new IllegalArgumentException("Current folder is not repo root: " + getRepoPath());
        }
        // delete all children
        delete();
        // mark root for deletion as well
        markForDeletion = true;
    }

    @Override
    protected boolean readyForPersistence() {
        return true;
    }

    @Override
    protected long doCreateNode() {
        return getFileService().createFolder(mutableInfo);
    }

    @Override
    protected void doUpdateNode() {
        getFileService().updateFolder(id, mutableInfo);
    }
}
