package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_SUPPRESS_POM_CHECKS;

/**
 * @author Dan Feldman
 */
public class IvyTypeSpecificConfigModel extends MavenTypeSpecificConfigModel {

    //local
    private Boolean suppressPomConsistencyChecks = DEFAULT_SUPPRESS_POM_CHECKS;

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
        return RepoType.Ivy;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
