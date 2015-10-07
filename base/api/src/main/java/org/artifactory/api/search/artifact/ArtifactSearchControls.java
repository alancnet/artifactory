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

package org.artifactory.api.search.artifact;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.SearchControlsBase;

/**
 * Search controls to search for files by name pattern.
 */
public class ArtifactSearchControls extends SearchControlsBase {

    private String query;
    private String relativePath;

    public ArtifactSearchControls() {
    }

    /**
     * Copy constructor
     *
     * @param artifactSearchControls Controls to copy
     */
    public ArtifactSearchControls(ArtifactSearchControls artifactSearchControls) {
        this.query = artifactSearchControls.query;
        this.relativePath = artifactSearchControls.relativePath;
        setSelectedRepoForSearch(artifactSearchControls.selectedRepoForSearch);
        setLimitSearchResults(artifactSearchControls.isLimitSearchResults());
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Relative path to search the artifact under
     *
     * @param relativePath Relative path to search the artifact under
     * @see org.artifactory.sapi.search.VfsQuery#addPathFilter(String)
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(query);
    }

    @Override
    public boolean isWildcardsOnly() {
        return isWildcardsOnly(query);
    }
}