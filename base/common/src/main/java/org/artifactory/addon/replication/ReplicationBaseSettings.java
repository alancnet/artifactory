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

package org.artifactory.addon.replication;

import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public abstract class ReplicationBaseSettings {

    private final RepoPath repoPath;
    private final boolean deleteExisting;
    private final boolean includeProperties;
    private final String url;
    private final int socketTimeoutMillis;

    protected ReplicationBaseSettings(RepoPath repoPath, boolean deleteExisting, boolean includeProperties,
            String url, int socketTimeoutMillis) {
        this.repoPath = repoPath;
        this.deleteExisting = deleteExisting;
        this.includeProperties = includeProperties;
        this.url = url;
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public boolean isDeleteExisting() {
        return deleteExisting;
    }

    public boolean isIncludeProperties() {
        return includeProperties;
    }

    public String getUrl() {
        return url;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReplicationBaseSettings)) {
            return false;
        }

        ReplicationBaseSettings that = (ReplicationBaseSettings) o;

        if (deleteExisting != that.deleteExisting) {
            return false;
        }
        if (includeProperties != that.includeProperties) {
            return false;
        }
        if (socketTimeoutMillis != that.socketTimeoutMillis) {
            return false;
        }
        if (!repoPath.equals(that.repoPath)) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = repoPath.hashCode();
        result = 31 * result + (deleteExisting ? 1 : 0);
        result = 31 * result + (includeProperties ? 1 : 0);
        result = 31 * result + socketTimeoutMillis;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
