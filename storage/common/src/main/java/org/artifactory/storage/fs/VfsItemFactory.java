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

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.MutableVfsFile;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author freds
 * @date Nov 17, 2008
 */
//TORE: [by YS] move some methods to other interfaces and rename this interface
public interface VfsItemFactory {
    String getKey();

    boolean isLocal();

    boolean isReal();

    boolean isCache();

    boolean isSuppressPomConsistencyChecks();

    ModuleInfo getItemModuleInfo(String relativePath);

    ChecksumPolicy getChecksumPolicy();

    boolean itemExists(String subPath);

    @Nonnull
    MutableVfsFile createOrGetFile(RepoPath repoPath);

    @Nullable
    VfsItem getImmutableFsItem(RepoPath repoPath);

    @Nullable
    MutableVfsFile getMutableFile(RepoPath repoPath);

    @Nullable
    MutableVfsItem getMutableFsItem(RepoPath repoPath);

    @Nullable
    VfsFile getImmutableFile(RepoPath repoPath);

    @Nonnull
    MutableVfsFolder createOrGetFolder(RepoPath repoPath);

    @Nullable
    VfsFolder getImmutableFolder(RepoPath repoPath);

    @Nullable
    MutableVfsFolder getMutableFolder(RepoPath repoPath);
}
