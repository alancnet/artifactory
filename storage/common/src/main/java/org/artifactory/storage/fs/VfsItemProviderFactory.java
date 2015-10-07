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

package org.artifactory.storage.fs;

import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.repo.StoringRepo;

/**
 * A factory {@link VfsItemProvider}.
 *
 * @author Yossi Shaul
 */
public interface VfsItemProviderFactory {
    VfsItemProvider createItemProvider(StoringRepo storingRepo, RepoPath repoPath, FsItemsVault fsItemsVault);

    VfsFileProvider createFileProvider(StoringRepo storingRepo, RepoPath repoPath, FsItemsVault fsItemsVault);

    VfsFolderProvider createFolderProvider(StoringRepo storingRepo, RepoPath repoPath, FsItemsVault fsItemsVault);
}
