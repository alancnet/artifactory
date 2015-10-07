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

import java.util.ArrayList;
import java.util.List;

/**
 * Json object retuning the UsageSinceResource search results
 *
 * @author Eli Givoni
 */
public class LastDownloadRestResult {
    public List<DownloadedEntry> results = new ArrayList<>();

    @Override
    public String toString() {
        return results.toString();
    }

    public static class DownloadedEntry {
        public String uri;
        public String lastDownloaded;

        public DownloadedEntry(String uri, String lastDownloaded) {
            this.uri = uri;
            this.lastDownloaded = lastDownloaded;
        }

        @SuppressWarnings("UnusedDeclaration")
        private DownloadedEntry() {
            // for json mapper
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("DownloadedEntry");
            sb.append("{uri='").append(uri).append('\'');
            sb.append(", lastDownloaded='").append(lastDownloaded).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
