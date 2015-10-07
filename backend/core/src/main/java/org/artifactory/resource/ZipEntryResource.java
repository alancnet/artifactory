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

import org.artifactory.fs.ZipEntryRepoResource;
import org.artifactory.fs.ZipEntryResourceInfo;
import org.artifactory.repo.RepoPath;

/**
 * This downloadable resource represents a file inside a zip/jar resource.
 * <p/>
 *
 * @author Yossi Shaul
 */
public class ZipEntryResource implements ZipEntryRepoResource {
    private final ZipEntryResourceInfo info;
    private RepoPath responsePath;
    private boolean expirable;

    public ZipEntryResource(ZipEntryResourceInfo zipInfo) {
        this.info = zipInfo;
    }

    @Override
    public String getEntryPath() {
        return info.getEntryPath();
    }

    @Override
    public RepoPath getRepoPath() {
        return info.getRepoPath();
    }

    @Override
    public RepoPath getResponseRepoPath() {
        return responsePath != null ? responsePath : getRepoPath();
    }

    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
        this.responsePath = responsePath;
    }

    @Override
    public RepoResourceInfo getInfo() {
        return info;
    }

    @Override
    public boolean isFound() {
        return true;
    }

    @Override
    public boolean isExactQueryMatch() {
        return true;
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
        return info.getSize();
    }

    @Override
    public long getCacheAge() {
        // The age is always the age of the zip file itself
        long lastUpdated = info.getZipFileInfo().getLastUpdated();
        if (lastUpdated <= 0) {
            return -1;
        }
        return System.currentTimeMillis() - lastUpdated;
    }

    @Override
    public long getLastModified() {
        return info.getLastModified();
    }

    @Override
    public String getMimeType() {
        return info.getMimeType();
    }

    @Override
    public boolean isExpirable() {
        return expirable;
    }

    @Override
    public void expirable() {
        expirable = true;
    }
}
