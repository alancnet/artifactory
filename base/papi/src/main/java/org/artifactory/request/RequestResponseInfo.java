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

package org.artifactory.request;

import org.artifactory.repo.RepoPath;

import java.io.Serializable;

/**
 * NOTE: INTERNAL USE ONLY - NOT PART OF THE PUBLIC API!
 *
 * @author yoavl
 */
public interface RequestResponseInfo extends Serializable {

    RequestContext getRequestContext();

    /**
     * @return The resource repository path. This path might be virtual and is usually represent the path from the
     *         request.
     */
    RepoPath getRequestRepoPath();

    /**
     * @return The actual repo path the resource came from. Might be different from the request repo path. For example
     *         when a request is made on a virtual repository, the response repo path should point to the actual
     *         repository containing this resource.
     */
    RepoPath getResponseRepoPath();

    String getRemoteRepoUrl();

    boolean isFound();

    boolean isExactQueryMatch();

    boolean isExpired();

    boolean isMetadata();

    long getCacheAge();

    String getMimeType();

    String getName();

    long getLastModified();

    long getSize();

    void setSize(long size);

    String getSha1();

    String getMd5();
}
