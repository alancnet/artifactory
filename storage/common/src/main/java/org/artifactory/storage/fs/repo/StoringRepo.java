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

package org.artifactory.storage.fs.repo;

import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.MutableVfsFolder;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;

import java.util.List;

/**
 * Interface for repositories that store real artifacts.
 *
 * @author Yossi Shaul
 */
public interface StoringRepo {

    /**
     * @return The repository key
     */
    public String getKey();

    /**
     * @param mutableFolder The mutable folder
     * @return A mutable (write locked) list of direct descendants of the current mutable folder
     */
    List<MutableVfsItem> getMutableChildren(MutableVfsFolder mutableFolder);

    /**
     * @return True if an item with this relative path exists in this storing repository.
     */
    boolean itemExists(String relativePath);

    List<VfsItem> getImmutableChildren(VfsFolder folder);

    boolean hasChildren(VfsFolder vfsFolder);

    VfsFolder getImmutableFolder(RepoPath repoPath);

    boolean isWriteLocked(RepoPath repoPath);
}
