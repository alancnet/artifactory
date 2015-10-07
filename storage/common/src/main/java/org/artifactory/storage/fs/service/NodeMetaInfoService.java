/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;

/**
 * A service to retrieve metadata on nodes.
 *
 * @author Yossi Shaul
 */
public interface NodeMetaInfoService {
    /**
     * @param repoPath Repo path to retrieve meta info.
     * @return The meta info of the specified repo path. Null if none exist.
     */
    @Nullable
    ItemMetaInfo getNodeMetaInfo(RepoPath repoPath);

    /**
     * Creates or updates the metadata info of the given node.
     *
     * @param nodeId   The node id
     * @param metaInfo The new meta info
     */
    void createOrUpdateNodeMetaInfo(long nodeId, ItemMetaInfo metaInfo);

    /**
     * Deletes the meta info of the specified repo path.
     *
     * @param repoPath The repo path to delete meta info from
     */
    void deleteMetaInfo(RepoPath repoPath);

    void deleteMetaInfo(long nodeId);

    /**
     * @param repoPath The item repo path
     * @return True if the specified repo path has meta info. False if doesn't not exist.
     */
    boolean hasNodeMetadata(RepoPath repoPath);
}
