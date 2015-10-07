package org.artifactory.ui.rest.model.artifacts.deploy;

import org.artifactory.rest.common.model.BaseModel;

/**
 * Describes a Response from UI Deploy
 *
 * @author Aviad Shikloshi
 */
public class UploadedArtifactInfo extends BaseModel {

    private Boolean showUrl;
    private String repoKey;
    private String artifactPath;

    public UploadedArtifactInfo() {
    }

    public UploadedArtifactInfo(Boolean showUrl, String repoKey, String artifactPath) {
        this.showUrl = showUrl;
        this.repoKey = repoKey;
        this.artifactPath = artifactPath;
    }

    public Boolean isShowUrl() {
        return showUrl;
    }

    public void setShowUrl(Boolean showUrl) {
        this.showUrl = showUrl;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }
}
