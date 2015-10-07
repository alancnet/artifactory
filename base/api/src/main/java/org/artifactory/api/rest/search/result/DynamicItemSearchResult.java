/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 5/11/14 2:51 PM
 *
 * @author freds
 */
public class DynamicItemSearchResult {
    public List<SearchEntry> results = new ArrayList<>();

    public static class SearchEntry {
        public String uri;
        public String created;
        public String lastModified;
        public String lastDownloaded;
    }
}
