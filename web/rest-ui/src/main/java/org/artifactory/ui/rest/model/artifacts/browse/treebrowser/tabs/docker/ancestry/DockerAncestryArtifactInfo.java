package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.ancestry;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

public class DockerAncestryArtifactInfo extends BaseArtifactInfo {

    private DockerLinkedImage dockerLinkedImage;

    public DockerLinkedImage getDockerLinkedImage() {
        return dockerLinkedImage;
    }

    public void setDockerLinkedImage(
            DockerLinkedImage dockerLinkedImage) {
        this.dockerLinkedImage = dockerLinkedImage;
    }
}
