package org.artifactory.addon.plugin.download;

import org.artifactory.addon.plugin.PluginAction;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;

/**
 * Intercept any before download request (from any repo), the user is given a {@link DownloadCtx} to set
 * custom properties on it to be used as part of the download action.
 *
 * @author Shay Yaakov
 * @see DownloadCtx
 */
public interface BeforeDownloadRequestAction extends PluginAction {
    void beforeDownloadRequest(Request request, RepoPath repoPath);
}