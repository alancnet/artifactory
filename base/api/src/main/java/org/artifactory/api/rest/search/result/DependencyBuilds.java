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

package org.artifactory.api.rest.search.result;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class DependencyBuilds {

    List<DependencyBuild> results;

    public DependencyBuilds(List<DependencyBuild> results) {
        this.results = results;
    }

    public DependencyBuilds() {
    }

    public List<DependencyBuild> getResults() {
        return results;
    }

    public void setResults(List<DependencyBuild> results) {
        this.results = results;
    }

    public static class DependencyBuild {
        private String uri;

        public DependencyBuild(String uri) {
            this.uri = uri;
        }

        public DependencyBuild() {
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }
}
