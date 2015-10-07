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

import org.artifactory.fs.WatcherInfo;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.model.WatcherRepoPathInfo;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A business service to interact with the node watches table.
 *
 * @author Yossi Shaul
 */
public interface WatchesService {
    @Nonnull
    WatchersInfo getWatches(RepoPath repoPath);

    boolean hasWatches(RepoPath repoPath);

    @Nonnull
    int addWatch(long nodeId, WatcherInfo watchInfo);

    //void setWatches(long nodeId, List<WatcherInfo> watches);

    int deleteWatches(long nodeId);

    /**
     * Deletes the user watches of this node. Normally, user has at most one watch on the node.
     *
     * @param repoPath The repo path to delete user watches from
     * @param username The username with the watch
     * @return Number of watches deleted
     */
    int deleteUserWatches(RepoPath repoPath, String username);

    /**
     * Deletes all the user watches.
     *
     * @param username The username with the watch
     * @return Number of watches deleted
     */
    int deleteAllUserWatches(String username);

    /**
     * Adds watches to this node.
     *
     * @param nodeId  The node id to set watches on.
     * @param watches The watches to add.
     */
    void addWatches(long nodeId, List<WatcherInfo> watches);

    @Nonnull
    List<WatcherRepoPathInfo> loadWatches();

    /**
     * check if user is currently watching this repo path
     * @param repoPath - repo path
     * @param userName - user Name
     * @return if true , user is watching
     */
    boolean isUserWatchingRepoPath(RepoPath repoPath,String userName);

}
