package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.includedRepositories.IncludedRepositories;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.RepositoryInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
public class VirtualRemoteRepoGeneralArtifactInfo extends BaseArtifactInfo {

    private BaseInfo info;
    private IncludedRepositories includedRepositories;
    private String offlineMessage;
    private String blackedOutMessage;

    public VirtualRemoteRepoGeneralArtifactInfo() {
    }

    public VirtualRemoteRepoGeneralArtifactInfo(String name) {
        super(name);
    }

    public void populateGeneralData(RepoBaseDescriptor repoBaseDescriptor, HttpServletRequest request) {
        RepoPath repoPath = InternalRepoPathFactory.create(repoBaseDescriptor.getKey(), "");
        // update general info
        BaseInfo baseInfo = populateVirtualRemoteRepositoryInfo(repoBaseDescriptor, repoPath);
        // update included repositories data
        updateVirtualIncludedRepositories(repoBaseDescriptor, request);

        this.info = baseInfo;
    }

    /**
     * populate Repository info data
     *
     * @param repoDescriptor - repo descriptor
     * @param repoPath       - repo path
     * @return
     */
    private BaseInfo populateVirtualRemoteRepositoryInfo(RepoBaseDescriptor repoDescriptor, RepoPath repoPath) {
        RepositoryInfo repoInfo = new RepositoryInfo();
        if (repoDescriptor instanceof VirtualRepoDescriptor) {
            VirtualRepoDescriptor virtualRepoDescriptor = (VirtualRepoDescriptor) repoDescriptor;
            repoInfo.populateVirtualRepositoryInfo(virtualRepoDescriptor, repoPath);
        } else {
            repoInfo.populateRemoteRepositoryInfo(repoDescriptor, repoPath);
        }
        setRepositoryOffline(repoDescriptor);
        setRepositoryBlackedOut(repoDescriptor);
        return repoInfo;
    }

    /**
     * update included repositories data
     *
     * @param repoDescriptor - repo descriptor
     * @param request        - http servlet request
     */
    private void updateVirtualIncludedRepositories(RepoBaseDescriptor repoDescriptor, HttpServletRequest request) {
        if (repoDescriptor instanceof VirtualRepoDescriptor) {
            VirtualRepoDescriptor virtualRepoDescriptor = (VirtualRepoDescriptor) repoDescriptor;
            updateIncludedRepositories(virtualRepoDescriptor, request);
        }
    }


    /**
     * update included repositories data
     *
     * @param virtualRepoDescriptor - virtual repo descriptor
     * @param request
     */
    private void updateIncludedRepositories(VirtualRepoDescriptor virtualRepoDescriptor, HttpServletRequest request) {
        IncludedRepositories includedRepositories = new IncludedRepositories(virtualRepoDescriptor.getRepositories(),
                request);
        this.includedRepositories = includedRepositories;
    }

    /**
     * set repositories  offline data
     *
     * @param repoDescriptor - repo descriptor
     */
    private void setRepositoryOffline(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor instanceof HttpRepoDescriptor) {
            if (((HttpRepoDescriptor) repoDescriptor).isOffline()) {
                this.setOfflineMessage("This repository is offline, content is served from the cache only.");
            }
        }
    }

    private void setRepositoryBlackedOut(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor instanceof RealRepoDescriptor) {
            if (((RealRepoDescriptor) repoDescriptor).isBlackedOut()) {
                this.setBlackedOutMessage("This repository is blacked out, " +
                        "items can only be viewed but cannot be resolved or deployed.");
            }
        }
    }

    public BaseInfo getInfo() {
        return info;
    }

    public void setInfo(BaseInfo info) {
        this.info = info;
    }

    public String getOfflineMessage() {
        return offlineMessage;
    }

    public void setOfflineMessage(String offlineMessage) {
        this.offlineMessage = offlineMessage;
    }

    public String getBlackedOutMessage() {
        return blackedOutMessage;
    }

    public void setBlackedOutMessage(String blackedOutMessage) {
        this.blackedOutMessage = blackedOutMessage;
    }

    public String toString() {
        return JsonUtil.jsonToString(this);
    }

    public IncludedRepositories getIncludedRepositories() {
        return includedRepositories;
    }

    public void setIncludedRepositories(
            IncludedRepositories includedRepositories) {
        this.includedRepositories = includedRepositories;
    }
}
