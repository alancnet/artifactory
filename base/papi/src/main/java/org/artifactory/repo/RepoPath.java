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

package org.artifactory.repo;

import org.artifactory.common.Info;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Holds a compound path of a repository id and a path withing that repository separated by ':'
 */
public interface RepoPath extends Info {
    char REPO_PATH_SEP = ':';
    char ARCHIVE_SEP = '!';
    String REMOTE_CACHE_SUFFIX = "-cache";

    @Nonnull
    String getRepoKey();

    String getPath();

    String getId();

    /**
     * A path composed of the repository key and path.
     * <pre>
     * repoKey = "key", path = "path/to" returns "key/path/to"
     * repoKey = "key", name = "" returns "key/"
     * </pre>
     *
     * @return A path composed of the repository key and path
     */
    String toPath();

    /**
     * @return The name of the path as if it was a file (the string after the last '/' or '\')
     */
    String getName();

    /**
     * @return Parent repo path of this path. Null if current is root repository path).
     */
    @Nullable
    RepoPath getParent();

    /**
     * @return True if this repo path points to the root (i.e., the ath part is empty)
     */
    boolean isRoot();

    /**
     * @return True if this repo path represents a file
     */
    boolean isFile();

    /**
     * @return True if this repo path represents a folder
     */
    boolean isFolder();
}
