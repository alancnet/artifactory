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

package org.artifactory.sapi.fs;

import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;

/**
 * An immutable interface of a virtual file/folder.
 *
 * @author Yossi Shaul
 */
public interface VfsItem<T extends ItemInfo> {
    /**
     * @return The storage system id of this item. Zero if the item is not persisted.
     */
    long getId();

    /**
     * @return The path part of the repo path (i.e., path relative to the repo key)
     */
    String getPath();

    String getName();

    String getRepoKey();

    RepoPath getRepoPath();

    boolean isFile();

    boolean isFolder();

    long getCreated();

    ItemInfo getInfo();

    @Nonnull
    Properties getProperties();
}
