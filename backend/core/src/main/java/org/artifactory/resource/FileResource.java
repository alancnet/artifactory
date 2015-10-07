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

import org.apache.commons.lang.StringUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;

/**
 * @author Yoav Landman
 */
public class FileResource implements RepoResource {

    private final MutableFileInfo info;
    /**
     * Response repo path represents the exact repo path the resource came from which might be different from the
     * request repo path. An example is when file is requested from remote repo and served from the remote cache or
     * request from virtual and the response is coming from local/cache.
     */
    private RepoPath responseRepoPath;
    private boolean exactQueryMatch;
    private String mimeType;
    private boolean expirable;

    public FileResource(FileInfo fileInfo) {
        this(fileInfo, true);
    }

    public FileResource(FileInfo fileInfo, boolean exactQueryMatch) {
        // create mutable copy of the file info. this will guarantee that changes done to the resource file info
        // doesn't change the original (possibly immutable) file info
        this.info = InfoFactoryHolder.get().copyFileInfo(fileInfo);
        this.exactQueryMatch = exactQueryMatch;
    }

    @Override
    public RepoPath getRepoPath() {
        return info.getRepoPath();
    }

    /**
     * @see FileResource#responseRepoPath
     */
    @Override
    public RepoPath getResponseRepoPath() {
        return responseRepoPath != null ? responseRepoPath : getRepoPath();
    }

    /**
     * @see FileResource#responseRepoPath
     */
    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
        this.responseRepoPath = responsePath;
    }

    @Override
    public MutableFileInfo getInfo() {
        return info;
    }

    @Override
    public boolean isFound() {
        return true;
    }

    @Override
    public boolean isExactQueryMatch() {
        return exactQueryMatch;
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
    public long getLastModified() {
        return info.getLastModified();
    }

    @Override
    public String getMimeType() {
        if (StringUtils.isNotBlank(mimeType)) {
            return mimeType;
        } else {
            MimeType contentType = NamingUtils.getMimeType(info.getRelPath());
            return contentType.getType();
        }
    }

    /**
     * Set custom mime type for this resource. If not set the mime types file is consulted.
     *
     * @param mimeType Custom mime type to set on this resource
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public long getCacheAge() {
        long lastUpdated = info.getLastUpdated();
        if (lastUpdated <= 0) {
            return -1;
        }
        return System.currentTimeMillis() - lastUpdated;
    }

    @Override
    public boolean isExpirable() {
        return expirable;
    }

    @Override
    public void expirable() {
        expirable = true;
    }

    @Override
    public String toString() {
        return info.getRepoPath().toString();
    }
}
