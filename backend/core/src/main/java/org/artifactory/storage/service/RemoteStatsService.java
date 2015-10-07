package org.artifactory.storage.service;

import org.artifactory.repo.RepoPath;

/**
 * Created by Michael Pasternak on 8/25/15.
 */
public interface RemoteStatsService {
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
    void fileDownloaded(String origin, RepoPath repoPath, String downloadedBy, long downloadedTime, long count);

    /**
     * Flushes the collected statistics event from the memory to the backing storage.
     */
    void flushStats();
}
