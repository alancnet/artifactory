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

package org.artifactory.util;

import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

import javax.annotation.Nullable;

/**
 * Utility class for {@link org.artifactory.repo.RepoPath}.
 *
 * @author Yossi Shaul
 */
public abstract class RepoPathUtils {

    /**
     * @param degree The degree of the ancestor (1 - parent, 2 - grandparent, etc)
     * @return Returns the n-th ancestor of this repo path. Null if doesn't exist.
     */
    @Nullable
    public static RepoPath getAncestor(RepoPath repoPath, int degree) {
        RepoPath result = repoPath.getParent();   // first ancestor
        for (int i = degree - 1; i > 0 && result != null; i--) {
            result = result.getParent();
        }
        return result;
    }

    /**
     * Creates repo path representing the root repository path (i.e. path is empty).
     *
     * @param repoKey The repository key
     * @return Repository root repo path
     */
    public static RepoPath repoRootPath(String repoKey) {
        return RepoPathFactory.create(repoKey, "");
    }
}
