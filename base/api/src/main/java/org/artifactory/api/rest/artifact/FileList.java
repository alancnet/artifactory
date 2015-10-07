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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * File list REST command result object
 *
 * @author Noam Y. Tenne
 */
public class FileList implements Serializable {

    String uri;
    String created;
    List<FileListElement> files;

    /**
     * Default constructor
     */
    public FileList() {
    }

    /**
     * Full constructor
     *
     * @param uri     URI of request sent by user
     * @param created The ISO8601 time the result was assembled
     * @param files   List of folders found
     */
    public FileList(@Nullable String uri, String created, List<FileListElement> files) {
        this.uri = uri;
        this.created = created;
        this.files = files;
    }

    /**
     * Returns the URI of the request
     *
     * @return Request URI
     */
    @Nullable
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI of the request
     *
     * @param uri Request URI
     */
    public void setUri(@Nullable String uri) {
        this.uri = uri;
    }

    /**
     * Returns the creation time of the result
     *
     * @return Result creation time
     */
    public String getCreated() {
        return created;
    }

    /**
     * Sets the creation time of the result
     *
     * @param created Result creation time
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * Returns the list of files found
     *
     * @return Found file list
     */
    public List<FileListElement> getFiles() {
        return files;
    }

    /**
     * List of files found
     *
     * @param files Found file list
     */
    public void setFiles(List<FileListElement> files) {
        this.files = files;
    }
}
