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

package org.artifactory.resource;

import org.apache.http.HttpStatus;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RepoPath;

public class ExpiredRepoResource implements RepoResource, UnfoundRepoResourceReason {

    private RepoResource wrappedResource;

    public ExpiredRepoResource(RepoResource wrappedResource) {
        this.wrappedResource = wrappedResource;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.SC_NOT_FOUND;
    }

    @Override
    public String getDetail() {
        return "Resource has expired";
    }

    @Override
    public RepoPath getRepoPath() {
        return wrappedResource.getRepoPath();
    }

    @Override
    public RepoPath getResponseRepoPath() {
        return wrappedResource.getResponseRepoPath();
    }

    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
        wrappedResource.setResponseRepoPath(responsePath);
    }

    @Override
    public RepoResourceInfo getInfo() {
        return wrappedResource.getInfo();
    }

    @Override
    public long getSize() {
        return wrappedResource.getSize();
    }

    @Override
    public long getCacheAge() {
        return wrappedResource.getCacheAge();
    }

    @Override
    public long getLastModified() {
        return wrappedResource.getLastModified();
    }

    @Override
    public String getMimeType() {
        return wrappedResource.getMimeType();
    }

    @Override
    public boolean isFound() {
        return false;
    }

    @Override
    public boolean isExactQueryMatch() {
        return wrappedResource.isExactQueryMatch();
    }

    @Override
    public boolean isExpired() {
        return true;
    }

    @Override
    public boolean isMetadata() {
        return wrappedResource.isMetadata();
    }

    @Override
    public boolean isExpirable() {
        return true;
    }

    @Override
    public void expirable() {
    }

    @Override
    public String toString() {
        return wrappedResource.getRepoPath().toString();
    }

    @Override
    public Reason getReason() {
        return Reason.EXPIRED;
    }

}