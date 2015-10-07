package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_BOWER_REGISTRY;

/**
 * @author Dan Feldman
 */
public class BowerTypeSpecificConfigModel extends VcsTypeSpecificConfigModel {

    //remote
    protected String registryUrl = DEFAULT_BOWER_REGISTRY;

    public String getRegistryUrl() {
        return registryUrl;
    }

    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Bower;
    }

    @Override
    public String getUrl() {
        return RepoConfigDefaultValues.VCS_URL;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
