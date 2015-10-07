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

import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFolderInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.fs.VfsFolderProvider;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.lock.LockingHelper;
import org.artifactory.storage.fs.repo.StoringRepo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provider of VFS files.
 *
 * @author Yossi Shaul
 */
public class DbFolderProvider extends DbFsItemProvider implements VfsFolderProvider {

    public DbFolderProvider(StoringRepo storingRepo, RepoPath repoPath, FsItemsVault vault) {
        super(storingRepo, repoPath, vault);
    }

    @Override
    @Nullable
    public VfsFolder getImmutableFolder() {
        VfsItem immutableFsItem = super.getImmutableFsItem();
        if (immutableFsItem != null && !immutableFsItem.isFolder()) {
            throw new FolderExpectedException(getRepoPath());
        }
        return (VfsFolder) immutableFsItem;
    }

    @Override
    @Nullable
    public MutableVfsFolder getMutableFolder() {
        VfsItem mutableFsItem = super.getMutableFsItem();
        if (mutableFsItem != null && !mutableFsItem.isFolder()) {
            LockingHelper.removeLockEntry(getRepoPath());
            throw new FolderExpectedException(getRepoPath());
        }
        return (MutableVfsFolder) mutableFsItem;
    }

    @Override
    @Nonnull
    public MutableVfsFolder getOrCreMutableFolder() {
        MutableVfsItem mutableFsItem = super.getOrCreateMutableFsItem();
        if (mutableFsItem.isFile()) {
            LockingHelper.removeLockEntry(getRepoPath());
            throw new FolderExpectedException(getRepoPath());
        }
        return (MutableVfsFolder) mutableFsItem;
    }

    @Override
    protected MutableVfsFolder createNewMutableItem(StoringRepo storingRepo, RepoPath repoPath) {
        MutableFolderInfo folderInfo = InfoFactoryHolder.get().createFolderInfo(repoPath);
        return new DbMutableFolder(storingRepo, DbService.NO_DB_ID, folderInfo);
    }
}
