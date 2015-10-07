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

package org.artifactory.api.rest.search.query;

/**
 * REST API artifact query object
 *
 * @author Noam Y. Tenne
 */
public class RestArtifactQuery extends BaseRestQuery {

    private String artifactId;

    /**
     * Default constructor
     */
    public RestArtifactQuery() {
    }

    /**
     * Returns the artifact ID to search for
     *
     * @return Artifact ID to search for
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Sets the artifact ID to search for
     *
     * @param artifactId Artifact ID to search for
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
}