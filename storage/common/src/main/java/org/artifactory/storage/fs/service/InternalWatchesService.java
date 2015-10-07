package org.artifactory.storage.fs.service;

import org.artifactory.fs.WatcherInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author mamo
 */
public interface InternalWatchesService {
    //
    int internalAddWatch(long nodeId, WatcherInfo watchInfo);

    void internalDeleteAllWatchesForRepoPath(RepoPath repoPath);

    void internalDeleteUserWatchesFromCache(RepoPath repoPath, String username);

    void internalDeleteAllUserWatchesFromCache(String username);
}
