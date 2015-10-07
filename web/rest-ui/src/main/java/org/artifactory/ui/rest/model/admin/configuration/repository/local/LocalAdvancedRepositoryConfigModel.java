package org.artifactory.ui.rest.model.admin.configuration.repository.local;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.propertysets.PropertySetNameModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.AdvancedRepositoryConfigModel;

import java.util.List;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_ALLOW_CONTENT_BROWSING;
import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_BLACKED_OUT;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class LocalAdvancedRepositoryConfigModel implements AdvancedRepositoryConfigModel {

    protected List<PropertySetNameModel> propertySets;
    protected Boolean blackedOut = DEFAULT_BLACKED_OUT;
    protected Boolean allowContentBrowsing = DEFAULT_ALLOW_CONTENT_BROWSING;

    @Override
    public List<PropertySetNameModel> getPropertySets() {
        return propertySets;
    }

    @Override
    public void setPropertySets(List<PropertySetNameModel> propertySets) {
        this.propertySets = propertySets;
    }

    @Override
    public Boolean isBlackedOut() {
        return blackedOut;
    }

    @Override
    public void setBlackedOut(Boolean blackedOut) {
        this.blackedOut = blackedOut;
    }

    @Override
    public Boolean getAllowContentBrowsing() {
        return allowContentBrowsing;
    }

    @Override
    public void setAllowContentBrowsing(Boolean allowContentBrowsing) {
        this.allowContentBrowsing = allowContentBrowsing;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
