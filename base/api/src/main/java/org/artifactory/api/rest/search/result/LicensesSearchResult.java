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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * License search results for REST query.
 *
 * @author Tomer Cohen
 */
public class LicensesSearchResult {

    public List<ArtifactLicenses> results = Lists.newArrayList();

    public static class ArtifactLicenses {
        public String uri;
        public String license;
        public String found = "";
        public String status;
    }
}
