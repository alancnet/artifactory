package org.artifactory.addon.plugin.download;

import org.artifactory.repo.RepoPath;

/**
 * Download context filled up by a {@link BeforeDownloadRequestAction} plugin extension
 *
 * @author Shay Yaakov
 */
public class DownloadCtx {

    private boolean expired;

    private RepoPath modifiedRepoPath;

    /**
     * @return true if the resource being downloaded is marked as an expired resource,
     * see {@link #setExpired(boolean)} for full details about expired resources.
     */
    public boolean isExpired() {
        return expired;
    }

    /**
     * Whether the resource being downloaded is marked as expired by a user plugin.
     * When true, the cache expiry mechanism will treat this resource as expired regardless of it's last updated time.
     * This should be treated with caution, as it means both another database hit (for updating the last updated time)
     * as well as network overhead since if the resource is expired, a remote download will occur to re-download it to the cache.
     *
     * @param expired True if the resource being downloaded should be treated as expired
     */
    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    /**
     * The modified repo path provided by the user plugin for a given query
     * @return a modified repo path used inside Artifactory to answer the given request
     */
    public RepoPath getModifiedRepoPath() {
        return modifiedRepoPath;
    }

    /**
     * Override the actual repo path used inside the Artifactory download process to answer the given download request.
     * @param modifiedRepoPath The new repo path to use
     */
    public void setModifiedRepoPath(RepoPath modifiedRepoPath) {
        this.modifiedRepoPath = modifiedRepoPath;
    }
}
