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

import org.artifactory.fs.FolderInfo;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.fs.repo.StoringRepo;

import java.util.List;

/**
 * Virtual filesystem folder backed by a database record.
 *
 * @author Yossi Shaul
 */
public class DbFsFolder extends DbFsItem<FolderInfo> implements VfsFolder<FolderInfo> {

    public DbFsFolder(StoringRepo repo, long folderId, FolderInfo folderInfo) {
        super(repo, folderId, folderInfo);
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean hasChildren() {
        return getRepo().hasChildren(this);
    }

    @Override
    public List<VfsItem> getImmutableChildren() {
        return getRepo().getImmutableChildren(this);
    }
}
