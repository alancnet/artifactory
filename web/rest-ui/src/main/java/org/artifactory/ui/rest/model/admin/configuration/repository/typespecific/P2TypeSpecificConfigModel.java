package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.addon.p2.P2Repo;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

import java.util.List;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_SUPPRESS_POM_CHECKS;

/**
 * @author Dan Feldman
 */
public class P2TypeSpecificConfigModel extends MavenTypeSpecificConfigModel {

    //local
    private Boolean suppressPomConsistencyChecks = DEFAULT_SUPPRESS_POM_CHECKS;

    //virtual
    protected List<P2Repo> P2Repos; // TODO: [by dan] verify descriptor is not de\serialized

    public List<P2Repo> getP2Repos() {
        return P2Repos;
    }

    public void setP2Repos(List<P2Repo> P2Repos) {
        this.P2Repos = P2Repos;
    }

    @Override
    public Boolean getSuppressPomConsistencyChecks() {
        return this.suppressPomConsistencyChecks;
    }

    @Override
    public void setSuppressPomConsistencyChecks(Boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.P2;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
