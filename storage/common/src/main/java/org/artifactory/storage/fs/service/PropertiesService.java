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

package org.artifactory.storage.fs.service;

import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;

/**
 * A business service to interact with the node properties table.
 *
 * @author Yossi Shaul
 */
public interface PropertiesService {

    //TODO: [by YS] use properties info

    @Nonnull
    Properties getProperties(RepoPath repoPath);

    @Nonnull
    Properties loadProperties(long nodeId);

    boolean hasProperties(RepoPath repoPath);

    /**
     * Sets the given properties on the node id. Existing properties are overridden. An empty object will cause a
     * removal of all the properties on this node.
     *
     * @param nodeId     Id of the node to set properties on
     * @param properties The properties to set
     */
    void setProperties(long nodeId, Properties properties);

    int deleteProperties(long nodeId);
}
