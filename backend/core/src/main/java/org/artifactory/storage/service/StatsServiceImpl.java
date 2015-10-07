package org.artifactory.storage.service;

import org.artifactory.fs.StatsInfo;
import org.artifactory.model.xstream.fs.StatsImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.service.InternalStatsServiceImpl;
import org.artifactory.storage.fs.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

/**
 * Created by Michael Pasternak on 8/25/15.
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    InternalStatsServiceImpl internalStatsService;

    @Autowired
    RemoteStatsServiceImpl remoteStatsService;

    /**
     * Collects stats from both DB and queue of events
     *
     * @param repoPath The file repo path to get stats on
     *
     * @return {@link StatsInfo}
     */
    @Nullable
    @Override
    public StatsInfo getStats(RepoPath repoPath) {
        return (StatsImpl)internalStatsService.getStats(repoPath);
    }

    /**
     * Triggered on local download event
     *
     * Event queued for local stats update and potential delegation
     *
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     */
    @Override
    public void fileDownloaded(RepoPath repoPath, String downloadedBy, long downloadedTime) {
        internalStatsService.fileDownloaded(repoPath, downloadedBy, downloadedTime);
        remoteStatsService.fileDownloaded(repoPath, downloadedBy, downloadedTime);
    }

    /**
     * Triggered on remote download event
     *
     * Event queued for local stats update and potential delegation
     *
     * @param origin         The origin host
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     * @param count          Amount of performed downloads
     */
    @Override
    public void fileDownloadedRemotely(String origin, RepoPath repoPath, String downloadedBy,
            long downloadedTime, long count) {
        internalStatsService.fileDownloadedRemotely(origin, repoPath, downloadedBy, downloadedTime, count);
        remoteStatsService.fileDownloaded(origin, repoPath, downloadedBy, downloadedTime, count);
    }

    @Override
    public int setStats(long nodeId, StatsInfo statsInfo) {
        return internalStatsService.setStats(nodeId, statsInfo);
    }

    @Override
    public boolean deleteStats(long nodeId) {
        return internalStatsService.deleteStats(nodeId);
    }

    /**
     * Checks if local stats available
     *
     * @param repoPath The repo path to check
     * @return
     */
    @Override
    public boolean hasStats(RepoPath repoPath) {
        return internalStatsService.hasStats(repoPath);
    }

    /**
     * Performs all queues flash
     */
    @Override
    public void flushStats() {
        internalStatsService.flushStats();
        remoteStatsService.flushStats();
    }
}
