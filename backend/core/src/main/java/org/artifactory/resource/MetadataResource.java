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

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.md.MetadataInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.repo.RepoPath;

/**
 * @author Yoav Landman
 */
public class MetadataResource implements RepoResource {

    private final MetadataInfo info;
    private RepoPath responsePath;
    private boolean expirable = false;

    public MetadataResource(MetadataInfo info) {
        this.info = info;
    }

    public MetadataResource(RepoPath repoPath) {
        this.info = InfoFactoryHolder.get().createMetadata(repoPath);
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
    public MetadataInfo getInfo() {
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
        return true;
    }

    @Override
    public long getSize() {
        return info.getSize();
    }

    @Override
    public long getLastModified() {
        return info.getLastModified();
    }

    @Override
    public String getMimeType() {
        return MimeType.applicationXml;
    }

    @Override
    public long getCacheAge() {
        long lastUpdated = info.getLastModified();
        if (lastUpdated <= 0) {
            return -1;
        }
        return System.currentTimeMillis() - lastUpdated;
    }

    @Override
    public String toString() {
        return info.getRepoPath().toString();
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