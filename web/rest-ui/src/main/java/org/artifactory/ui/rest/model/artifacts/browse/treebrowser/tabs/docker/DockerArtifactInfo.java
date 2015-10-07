package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

/**
 * @author Chen Keinan
 */
public class DockerArtifactInfo extends BaseArtifactInfo {

    private DockerInfo dockerInfo;
    private DockerConfig dockerConfig;

    public DockerInfo getDockerInfo() {
        return dockerInfo;
    }

    public void setDockerInfo(
            DockerInfo dockerInfo) {
        this.dockerInfo = dockerInfo;
    }

    public DockerConfig getDockerConfig() {
        return dockerConfig;
    }

    public void setDockerConfig(
            DockerConfig dockerConfig) {
        this.dockerConfig = dockerConfig;
    }
}
