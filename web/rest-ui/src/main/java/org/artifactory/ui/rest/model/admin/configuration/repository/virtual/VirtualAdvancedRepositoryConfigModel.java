package org.artifactory.ui.rest.model.admin.configuration.repository.virtual;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_VIRTUAL_CAN_RETRIEVE_FROM_REMOTE;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonIgnoreProperties({"propertySets", "blackedOut", "allowContentBrowsing"})
public class VirtualAdvancedRepositoryConfigModel implements AdvancedRepositoryConfigModel {

    private Boolean retrieveRemoteArtifacts = DEFAULT_VIRTUAL_CAN_RETRIEVE_FROM_REMOTE;

    public Boolean getRetrieveRemoteArtifacts() {
        return retrieveRemoteArtifacts;
    }

    public void isRetreiveRemoteArtifacts(Boolean retreiveRemoteArtifacts) {
        this.retrieveRemoteArtifacts = retreiveRemoteArtifacts;
    }

    public void setRetrieveRemoteArtifacts(Boolean retrieveRemoteArtifacts) {
        this.retrieveRemoteArtifacts = retrieveRemoteArtifacts;
    }

    @Override
    public List<PropertySetNameModel> getPropertySets() {
        return null;
    }

    @Override
    public void setPropertySets(List<PropertySetNameModel> propertySets) {

    }

    @Override
    public Boolean isBlackedOut() {
        return null;
    }

    @Override
    public void setBlackedOut(Boolean blackedOut) {

    }

    @Override
    public Boolean getAllowContentBrowsing() {
        return null;
    }

    @Override
    public void setAllowContentBrowsing(Boolean allowContentBrowsing) {

    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
