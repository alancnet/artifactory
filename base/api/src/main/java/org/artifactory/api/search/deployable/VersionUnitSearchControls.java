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

package org.artifactory.api.search.deployable;

import org.artifactory.api.search.SearchControlsBase;
import org.artifactory.repo.RepoPath;

/**
 * Holds the version unit search parameters
 *
 * @author Noam Y. Tenne
 */
public class VersionUnitSearchControls extends SearchControlsBase {

    private RepoPath pathToSearchWithin;

    /**
     * Main constructor
     *
     * @param pathToSearchWithin Repo path to search version units within
     */
    public VersionUnitSearchControls(RepoPath pathToSearchWithin) {
        this.pathToSearchWithin = pathToSearchWithin;
        addRepoToSearch(pathToSearchWithin.getRepoKey());
    }

    /**
     * Returns the repo path to search within
     *
     * @return Repo path to search within
     */
    public RepoPath getPathToSearchWithin() {
        return pathToSearchWithin;
    }

    @Override
    public boolean isEmpty() {
        return pathToSearchWithin == null;
    }

    @Override
    public boolean isWildcardsOnly() {
        // cannot really be a wildcards only search
        return isEmpty();
    }

}
