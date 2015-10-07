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

package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;

/**
 * @author yoavl
 */
public interface RepoDescriptor extends Descriptor, Comparable {
    String getKey();

    RepoType getType();

    String getDescription();

    String getNotes();

    /**
     * @return A comma separated list of artifact patterns to include when evaluating requests.
     */
    String getIncludesPattern();

    /**
     * @return A comma separated list of artifact patterns to exclude when evaluating requests.
     */
    String getExcludesPattern();

    /**
     * @return True if not a virtual repository (for example remote cached or local)
     */
    boolean isReal();

    /**
     * Compare the parameters influencing the cache behavior, and return true if this and old descriptor have the same
     * behavior toward cache management.
     *
     * @param oldDescriptor the old descriptor
     * @return true if the cache can be use as is, false if the cache needs to be cleaned
     */
    boolean identicalCache(RepoDescriptor oldDescriptor);

    RepoLayout getRepoLayout();

    boolean isMavenRepoLayout();

    DockerApiVersion getDockerApiVersion();

    boolean isForceDockerAuthentication();

    boolean isForceNugetAuthentication();
}