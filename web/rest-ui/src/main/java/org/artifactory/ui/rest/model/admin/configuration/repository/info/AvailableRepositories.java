package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Aviad Shikloshi
 */
public class AvailableRepositories implements RestModel {

    private List<String> availableLocalRepos;
    private List<String> availableRemoteRepos;
    private List<String> availableVirtualRepos;

    public AvailableRepositories() {
    }

    public AvailableRepositories(List<String> availableLocalRepos, List<String> availableRemoteRepos,
            List<String> availableVirtualRepos) {
        this.availableLocalRepos = availableLocalRepos;
        this.availableRemoteRepos = availableRemoteRepos;
        this.availableVirtualRepos = availableVirtualRepos;
    }

    public List<String> getAvailableLocalRepos() {
        return availableLocalRepos;
    }

    public List<String> getAvailableRemoteRepos() {
        return availableRemoteRepos;
    }

    public List<String> getAvailableVirtualRepos() {
        return availableVirtualRepos;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
