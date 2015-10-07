package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.includedRepositories;

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

/**
 * @author Chen Keinan
 */
public class Repository {

    private String repoKey;
    private String linkUrl;
    private String type;

    public Repository() {
        // For Jackson
    }

    public Repository(RepoDescriptor repoDescriptor, String linkUrl) {
        this.repoKey = repoDescriptor.getKey();
        this.linkUrl = linkUrl;
        if (repoDescriptor instanceof VirtualRepoDescriptor) {
            this.type = "virtual";
        } else if (repoDescriptor instanceof RemoteRepoDescriptor) {
            this.type = "remote";
        } else if (repoDescriptor instanceof LocalRepoDescriptor) {
            this.type = "local";
        }
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
