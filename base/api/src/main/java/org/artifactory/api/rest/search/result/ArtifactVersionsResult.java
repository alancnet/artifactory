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

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Search result object to be returned by REST artifact versions searches
 *
 * @author Shay Yaakov
 */
public class ArtifactVersionsResult {
    private List<VersionEntry> results;

    /**
     * Default constructor for JSON parsing
     */
    public ArtifactVersionsResult() {
    }

    public ArtifactVersionsResult(Collection<VersionEntry> versionEntries, final Comparator<String> versionComparator) {
        if (versionEntries == null) {
            this.results = Collections.emptyList();
        } else {
            this.results = Lists.newArrayList(versionEntries);
            // sort according to the input string version comparator
            Collections.sort(results, new Comparator<VersionEntry>() {
                @Override
                public int compare(VersionEntry version1, VersionEntry version2) {
                    return versionComparator.compare(version2.getVersion(), version1.getVersion());
                }
            });
        }
    }

    @Nonnull
    public List<VersionEntry> getResults() {
        return results;
    }

    public void setResults(List<VersionEntry> results) {
        this.results = results;
    }
}
