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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumType;

import java.util.Set;

/**
 * Search result object to be returned by REST bad checksum searches
 *
 * @author Tomer Cohen
 */
public class BadChecksumRestSearchResult {

    private Set<SearchEntry> results = Sets.newHashSet();

    public BadChecksumRestSearchResult(Set<SearchEntry> results) {
        this.results = results;
    }

    public BadChecksumRestSearchResult() {
    }

    public Set<SearchEntry> getResults() {
        return results;
    }

    public void setResults(Set<SearchEntry> results) {
        this.results = results;
    }

    public void addResultAccordingToType(String uri, String clientChecksum, String serverChecksum, ChecksumType type) {
        results.add(SearchEntry.createSearchEntryAccordingToType(uri, serverChecksum, clientChecksum, type));
    }

    public static class SearchEntry {
        private String uri;
        private String serverMd5;
        private String clientMd5;
        private String serverSha1;
        private String clientSha1;

        public SearchEntry(String uri) {
            this.uri = uri;
        }

        public static SearchEntry createSearchEntryAccordingToType(String uri, String serverChecksum,
                String clientChecksum, ChecksumType type) {
            SearchEntry entry = new SearchEntry(uri);
            serverChecksum = StringUtils.isNotBlank(serverChecksum) ? serverChecksum : "";
            clientChecksum = StringUtils.isNotBlank(clientChecksum) ? clientChecksum : "";
            if (ChecksumType.md5.equals(type)) {
                entry.setServerMd5(serverChecksum);
                entry.setClientMd5(clientChecksum);
            } else if (ChecksumType.sha1.equals(type)) {
                entry.setServerSha1(serverChecksum);
                entry.setClientSha1(clientChecksum);
            } else {
                throw new AssertionError();
            }
            return entry;
        }

        private SearchEntry() {
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getServerMd5() {
            return serverMd5;
        }

        public void setServerMd5(String serverMd5) {
            this.serverMd5 = serverMd5;
        }

        public String getClientMd5() {
            return clientMd5;
        }

        public void setClientMd5(String clientMd5) {
            this.clientMd5 = clientMd5;
        }

        public String getServerSha1() {
            return serverSha1;
        }

        public void setServerSha1(String serverSha1) {
            this.serverSha1 = serverSha1;
        }

        public String getClientSha1() {
            return clientSha1;
        }

        public void setClientSha1(String clientSha1) {
            this.clientSha1 = clientSha1;
        }
    }
}
