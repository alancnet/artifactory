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

import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;

/**
 * A business service to interact with file statistics.
 *
 * @author Yossi Shaul
 */
public interface StatsService {

    /**
     * @param repoPath The file repo path to get stats on
     * @return The {@link org.artifactory.fs.StatsInfo} of this node. Null if non exist
     */
    @Nullable
    StatsInfo getStats(RepoPath repoPath);

    /**
     * Update the download stats and increment the count by one. The storage update is not immediate.
     *
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     */
    void fileDownloaded(RepoPath repoPath, String downloadedBy, long downloadedTime);

    /**
     * Update the download (performed at remote artifactory instance) stats and increment the count by one.
     * The storage update is not immediate.
     *
     * @param origin         The origin hosts
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     * @param count          Amount of performed downloads
     */
    void fileDownloadedRemotely(String origin, RepoPath repoPath, String downloadedBy, long downloadedTime, long count);

    /**
     * Sets the stats details on the given node. Existing statistics on this node are overridden.
     *
     * @param nodeId    The node id to set stats on.
     * @param statsInfo The stats info
     * @return Updates rows count. Any value other than 1 is an error
     */
    int setStats(long nodeId, StatsInfo statsInfo);

    /**
     * Deletes statistics info for the specified noe.
     *
     * @param nodeId The node id
     * @return True if statistics were deleted from the database/ False otherwise (node doesn't exist or has no stats).
     */
    boolean deleteStats(long nodeId);

    /**
     * @param repoPath The repo path to check
     * @return True if the item represented by this item has stats info. Folders never have stats info
     */
    boolean hasStats(RepoPath repoPath);

    /**
     * Flushes the collected statistics event from the memory to the backing storage.
     */
    void flushStats();
}
