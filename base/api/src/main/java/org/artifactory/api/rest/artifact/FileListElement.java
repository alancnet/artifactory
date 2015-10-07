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

package org.artifactory.api.rest.artifact;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * A found file element of the File List REST command
 *
 * @author Noam Y. Tenne
 */
public class FileListElement implements Serializable {

    String uri;
    long size;
    String lastModified;
    boolean folder;
    String sha1;
    Map<String, String> mdTimestamps;

    public FileListElement() {
    }

    /**
     * Full constructor
     *
     * @param uri          URI of file relative to the request path
     * @param size         Physical size of file in bytes
     * @param lastModified The ISO8601 time the file was last modified
     * @param folder       True if item is a folder
     */
    public FileListElement(String uri, long size, String lastModified, boolean folder) {
        this.uri = uri;
        this.size = size;
        this.lastModified = lastModified;
        this.folder = folder;
    }

    /**
     * Returns the URI of the file relative to the request path
     *
     * @return Request path-relative URI of file
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI of the file relative to the request path
     *
     * @param uri Request path-relative URI of file
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the physical size of the file in bytes
     *
     * @return File physical size in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the physical size of the file in bytes
     *
     * @param size File physical size in bytes
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns the time the file was last modified
     *
     * @return File last modified time
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * Sets the time the file was last modified
     *
     * @param lastModified File last modified time
     */
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Indicates whether this element is a folder
     *
     * @return True if a folder
     */
    public boolean isFolder() {
        return folder;
    }

    /**
     * Sets the folder indication
     *
     * @param folder True if a folder
     */
    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public Map<String, String> getMdTimestamps() {
        return mdTimestamps;
    }

    public void setMdTimestamps(Map<String, String> mdTimestamps) {
        this.mdTimestamps = mdTimestamps;
    }

    public void addMdTimestamp(String metadataName, String timestamp) {
        if (mdTimestamps == null) {
            mdTimestamps = Maps.newHashMap();
        }
        mdTimestamps.put(metadataName, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileListElement)) {
            return false;
        }

        FileListElement that = (FileListElement) o;

        if (folder != that.folder) {
            return false;
        }
        if (size != that.size) {
            return false;
        }
        if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null) {
            return false;
        }
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (folder ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return uri;
    }
}
