package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class DockerTypeSpecificConfigModel implements TypeSpecificConfigModel {

    protected Boolean forceDockerAuthentication = DEFAULT_FORCE_DOCKER_AUTH;

    //local
    protected DockerApiVersion dockerApiVersion = DEFAULT_DOCKER_API_VER;

    //remote
    protected Boolean enableTokenAuthentication = DEFAULT_TOKEN_AUTH;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;


    public DockerApiVersion getDockerApiVersion() {
        return dockerApiVersion;
    }

    public void setDockerApiVersion(DockerApiVersion dockerApiVersion) {
        this.dockerApiVersion = dockerApiVersion;
    }

    public Boolean isEnableTokenAuthentication() {
        return enableTokenAuthentication;
    }

    public void setEnableTokenAuthentication(Boolean enableTokenAuthentication) {
        this.enableTokenAuthentication = enableTokenAuthentication;
    }

    public Boolean isForceDockerAuthentication() {
        return forceDockerAuthentication;
    }

    public void setForceDockerAuthentication(Boolean forceDockerAuthentication) {
        this.forceDockerAuthentication = forceDockerAuthentication;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Docker;
    }

    @Override
    public String getUrl() {
        return RepoConfigDefaultValues.DOCKER_URL;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
