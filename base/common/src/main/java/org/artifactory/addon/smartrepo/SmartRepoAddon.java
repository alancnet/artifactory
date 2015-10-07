package org.artifactory.addon.smartrepo;

import org.artifactory.addon.Addon;
import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author Chen Keinan
 */
public interface SmartRepoAddon extends Addon {

    boolean supportRemoteStats();

    void fileDownloadedRemotely(StatsInfo statsInfo, String remoteHost, RepoPath repoPath);

}
