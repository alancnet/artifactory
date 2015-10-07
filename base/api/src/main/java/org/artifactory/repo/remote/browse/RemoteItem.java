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

package org.artifactory.repo.remote.browse;

import org.artifactory.util.PathUtils;

import javax.annotation.Nonnull;

/**
 * Holds information about a remote file or directory.
 *
 * @author Yossi Shaul
 */
public class RemoteItem {
    /**
     * Absolute URL of the remote item.
     */
    private final String url;
    /**
     * True if the remote item is a directory.
     */
    private final boolean directory;
    /**
     * Size of the item in bytes.
     */
    private final long size;
    /**
     * Last modification time in millis. 0 if unknown.
     */
    private final long lastModified;
    /**
     * The effective url for linking this item, overriding {@link #url}
     */
    private String effectiveUrl;

    public RemoteItem(@Nonnull String url, boolean directory) {
        this(url, directory, 0, 0);
    }

    public RemoteItem(@Nonnull String url, boolean directory, long size, long lastModified) {
        if (url == null) {
            throw new NullPointerException("URL cannot be null");
        }
        this.url = url;
        this.directory = directory;
        this.size = size;
        this.lastModified = lastModified;
    }

    /**
     * @return The absolute URL to the remote item.
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return True if the remote item represents a directory.
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * @return Size of the item in bytes. Returns 0 for directories.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return Last modification time in millis. 0 if unknown.
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * @return The name of the item (the last part of the URL after the last '/')
     */
    public String getName() {
        return PathUtils.getFileName(url);
    }

    public String getEffectiveUrl() {
        return effectiveUrl;
    }

    public void setEffectiveUrl(String effectiveUrl) {
        this.effectiveUrl = effectiveUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoteItem that = (RemoteItem) o;

        if (directory != that.directory) {
            return false;
        }
        if (!url.equals(that.url)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (directory ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RemoteItem");
        sb.append("{url='").append(url).append('\'');
        sb.append(", directory=").append(directory);
        sb.append(", size=").append(size);
        sb.append(", lastModified=").append(lastModified);
        sb.append('}');
        return sb.toString();
    }

}
