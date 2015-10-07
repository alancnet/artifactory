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

package org.artifactory.fs;

import org.artifactory.repo.RepoPath;
import org.artifactory.resource.RepoResourceInfo;

import java.io.Serializable;

/**
 * A resolution result, encapsulating the outcome of a resource request against a repo.
 *
 * @author yoavl
 */
public interface RepoResource extends Serializable {

    /**
     * @return The resource repository path. This path might be virtual and is usually represent the path from the
     * request.
     */
    RepoPath getRepoPath();

    /**
     * @return The actual repo path the resource came from. Might be different from the request repo path. For example
     * when a request is made on a virtual repository, the response repo path should point to the actual
     * repository containing this resource.
     */
    RepoPath getResponseRepoPath();

    void setResponseRepoPath(RepoPath responsePath);

    RepoResourceInfo getInfo();

    boolean isFound();

    boolean isExactQueryMatch();

    boolean isExpired();

    boolean isMetadata();

    long getSize();

    long getCacheAge();

    long getLastModified();

    String getMimeType();

    /**
     * Returns true if this resource represents a resource which might get expired (metadata, properties, non-unique
     * snapshots etc.). Expirable resources should prevent or limit http caching.
     *
     * @return True if this resource is expirable
     */
    boolean isExpirable();

    /**
     * Sets this resource as expirable to indicate how http caches should cache this resource.
     */
    void expirable();
}
