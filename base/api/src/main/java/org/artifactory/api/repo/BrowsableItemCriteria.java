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

package org.artifactory.api.repo;

import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public class BrowsableItemCriteria {

    private final RepoPath repoPath;
    private final boolean includeChecksums;
    private final boolean includeRemoteResources;
    private final Properties requestProperties;

    private BrowsableItemCriteria(RepoPath repoPath, boolean includeChecksums,
            boolean includeRemoteResources, Properties requestProperties) {
        this.repoPath = repoPath;
        this.includeChecksums = includeChecksums;
        this.includeRemoteResources = includeRemoteResources;
        this.requestProperties = requestProperties;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public boolean isIncludeChecksums() {
        return includeChecksums;
    }

    public boolean isIncludeRemoteResources() {
        return includeRemoteResources;
    }

    public Properties getRequestProperties() {
        return requestProperties;
    }

    public static class Builder {

        private RepoPath repoPath;
        private boolean includeChecksums = true;
        private boolean includeRemoteResources = true;
        private Properties requestProperties;

        public Builder(RepoPath repoPath) {
            this.repoPath = repoPath;
        }

        public Builder(BrowsableItemCriteria copy) {
            this.repoPath = copy.getRepoPath();
            this.includeChecksums = copy.isIncludeChecksums();
            this.includeRemoteResources = copy.isIncludeRemoteResources();
            this.requestProperties = copy.getRequestProperties();
        }

        public Builder repoPath(RepoPath repoPath) {
            this.repoPath = repoPath;
            return this;
        }

        public Builder includeChecksums(boolean includeChecksums) {
            this.includeChecksums = includeChecksums;
            return this;
        }

        public Builder includeRemoteResources(boolean includeRemoteResources) {
            this.includeRemoteResources = includeRemoteResources;
            return this;
        }

        public Builder requestProperties(Properties requestProperties) {
            this.requestProperties = requestProperties;
            return this;
        }

        public BrowsableItemCriteria build() {
            if (repoPath == null) {
                throw new IllegalArgumentException("Please provide a repo path.");
            }

            return new BrowsableItemCriteria(repoPath, includeChecksums, includeRemoteResources, requestProperties);
        }
    }
}
