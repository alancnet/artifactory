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

package org.artifactory.api.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.fs.FileInfo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Holds and manages list of search files. Instances of this class doesn't allow files with the same relative path.
 *
 * @author Yossi Shaul
 */
public class SavedSearchResults implements Serializable {
    private final String name;
    //private final List<FileInfo> results;
    private final Set<RepoAgnosticFileInfo> results;

    public SavedSearchResults(String name, List<FileInfo> results) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Empty results name is not allowed");
        }
        this.name = name;
        this.results = Sets.newHashSet();
        if (results != null) {
            // make a protective copy and remove duplicates if exist
            addAll(results);
        }
    }

    public String getName() {
        return name;
    }

    public void merge(SavedSearchResults toMerge) {
        results.addAll(toMerge.results);
    }

    public void subtract(SavedSearchResults toSubtract) {
        results.removeAll(toSubtract.results);
    }

    public void intersect(SavedSearchResults toIntersect) {
        results.retainAll(toIntersect.results); // Sets.retainAll <==> intersection
    }

    public void discardFromResult(SavedSearchResults toDiscardFromResult) {
        results.removeAll(toDiscardFromResult.results);
    }

    public ImmutableList<FileInfo> getResults() {
        ImmutableList.Builder<FileInfo> builder = ImmutableList.builder();
        for (RepoAgnosticFileInfo result : results) {
            builder.add(result.fileInfo);
        }
        return builder.build();
    }

    public void addAll(Collection<FileInfo> fileInfos) {
        results.addAll(toRepoAgnosticSet(fileInfos));
    }

    public void removeAll(Collection<FileInfo> fileInfos) {
        results.removeAll(toRepoAgnosticSet(fileInfos));
    }

    public int size() {
        return results.size();
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    boolean contains(FileInfo fileInfo) {
        return results.contains(new RepoAgnosticFileInfo(fileInfo));
    }

    void add(FileInfo fileInfo) {
        results.add(new RepoAgnosticFileInfo(fileInfo));
    }

    private Set<RepoAgnosticFileInfo> toRepoAgnosticSet(Collection<FileInfo> fileInfos) {
        Set<RepoAgnosticFileInfo> agnostics = Sets.newHashSet();
        for (FileInfo fileInfo : fileInfos) {
            agnostics.add(new RepoAgnosticFileInfo(fileInfo));
        }
        return agnostics;
    }

    /**
     * Holds a file info and ignores the source repository when comparing two file infos.
     */
    private static class RepoAgnosticFileInfo implements Serializable {
        private FileInfo fileInfo;

        private RepoAgnosticFileInfo(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        /**
         * files equality is driven by the relative path only. We use the relative path because the artifact may be
         * from different repos and we only display one of them
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RepoAgnosticFileInfo that = (RepoAgnosticFileInfo) o;

            if (fileInfo.getRelPath() != null ? !fileInfo.getRelPath().equals(that.fileInfo.getRelPath()) :
                    that.fileInfo.getRelPath() != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return fileInfo.getRelPath() != null ? fileInfo.getRelPath().hashCode() : 0;
        }
    }

}
