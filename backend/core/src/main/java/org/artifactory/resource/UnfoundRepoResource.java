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

public class UnfoundRepoResource implements RepoResource, UnfoundRepoResourceReason {

    private final RepoPath repoPath;
    private final String detail;
    private final int statusCode;
    private final Reason reason;

    public UnfoundRepoResource(RepoPath repoPath, String detail, Reason reason) {
        this(repoPath, reason, detail, HttpStatus.SC_NOT_FOUND);
    }

    public UnfoundRepoResource(RepoPath repoPath, String detail) {
        this(repoPath, detail, HttpStatus.SC_NOT_FOUND);
    }

    public UnfoundRepoResource(RepoPath repoPath, String detail, int statusCode) {
        this.repoPath = repoPath;
        this.detail = detail;
        this.statusCode = statusCode > 0 ? statusCode : HttpStatus.SC_NOT_FOUND;
        this.reason =  Reason.UNDEFINED;
    }

    public UnfoundRepoResource(RepoPath repoPath, Reason reason, String detail, int statusCode) {
        this.repoPath = repoPath;
        this.detail = detail;
        this.statusCode = statusCode > 0 ? statusCode : HttpStatus.SC_NOT_FOUND;
        this.reason = reason;
    }

    @Override
    public String getDetail() {
        return detail;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public RepoPath getResponseRepoPath() {
        return null;
    }

    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
    }

    @Override
    public MutableRepoResourceInfo getInfo() {
        return null;
    }

    @Override
    public boolean isFound() {
        return false;
    }

    @Override
    public boolean isExactQueryMatch() {
        return false;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isMetadata() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long getCacheAge() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public boolean isExpirable() {
        return false;
    }

    @Override
    public void expirable() {
    }

    public Reason getReason() {
        return reason;
    }
}