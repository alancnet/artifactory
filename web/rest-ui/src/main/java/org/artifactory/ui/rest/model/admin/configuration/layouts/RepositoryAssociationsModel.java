package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Lior Hasson
 */
public class RepositoryAssociationsModel extends BaseModel {
    List<String> localRepositories;
    List<String> remoteRepositories;
    List<String> virtualRepositories;

    public List<String> getLocalRepositories() {
        return localRepositories;
    }

    public void setLocalRepositories(List<String> localRepositories) {
        this.localRepositories = localRepositories;
    }

    public List<String> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(List<String> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public List<String> getVirtualRepositories() {
        return virtualRepositories;
    }

    public void setVirtualRepositories(List<String> virtualRepositories) {
        this.virtualRepositories = virtualRepositories;
    }
}
