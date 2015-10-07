/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.repo.cleanup;

import org.artifactory.repo.RepoPath;

/**
 * Represents an integration file candidate to be deleted, the actual comparison (equals and hashCode)
 * are done on the parent folder since this is the unique key we would like to save.
 * The file repo path is saved because we calculate the module info from it.
 *
 * @author Shay Yaakov
 */
public class IntegrationCleanupCandidate {
    private RepoPath fileRepoPath;

    public IntegrationCleanupCandidate(RepoPath fileRepoPath) {
        this.fileRepoPath = fileRepoPath;
    }

    public RepoPath getFileRepoPath() {
        return fileRepoPath;
    }

    public RepoPath getParentRepoPath() {
        return fileRepoPath.getParent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntegrationCleanupCandidate)) {
            return false;
        }

        IntegrationCleanupCandidate that = (IntegrationCleanupCandidate) o;

        if (!getParentRepoPath().equals(that.getParentRepoPath())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getParentRepoPath().hashCode();
    }
}
