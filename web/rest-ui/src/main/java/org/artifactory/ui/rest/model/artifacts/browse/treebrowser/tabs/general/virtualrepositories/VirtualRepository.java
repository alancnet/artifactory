package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.virtualrepositories;

/**
 * @author Shay Yaakov
 */
public class VirtualRepository {

    private String repoKey;
    private String linkUrl;

    public VirtualRepository() {
        // For Jackson
    }

    public VirtualRepository(String repoKey, String linkUrl) {
        this.repoKey = repoKey;
        this.linkUrl = linkUrl;
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
}
