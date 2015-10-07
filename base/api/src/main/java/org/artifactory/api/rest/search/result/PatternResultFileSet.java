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

import java.util.Set;

/**
 * The pattern searcher result model
 *
 * @author Noam Y. Tenne
 */
public class PatternResultFileSet {

    private String repoUri;
    private String sourcePattern;
    private Set<String> files = Sets.newHashSet();

    public PatternResultFileSet(String repoUri, String sourcePattern) {
        this.repoUri = repoUri;
        this.sourcePattern = sourcePattern;
    }

    public PatternResultFileSet(String repoUri, String sourcePattern, Set<String> files) {
        this.repoUri = repoUri;
        this.sourcePattern = sourcePattern;
        this.files = files;
    }

    public PatternResultFileSet() {
    }

    public String getRepoUri() {
        return repoUri;
    }

    public void setRepoUri(String repoUri) {
        this.repoUri = repoUri;
    }

    public String getSourcePattern() {
        return sourcePattern;
    }

    public void setSourcePattern(String sourcePattern) {
        this.sourcePattern = sourcePattern;
    }

    public Set<String> getFiles() {
        return files;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }

    public void addFile(String fileRelativePath) {
        if (files == null) {
            files = Sets.newHashSet();
        }

        files.add(fileRelativePath);
    }
}
