package org.artifactory.ui.rest.model.builds;

import java.util.Collection;
import java.util.Set;

import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class BuildLicenseModel extends BaseModel {

    private Collection<ModuleLicenseModel> licenses;
    private Set<ModuleLicenseModel> publishedModules;
    private Set<String> scopes;

    public BuildLicenseModel() {
    }

    public BuildLicenseModel(Collection<ModuleLicenseModel> values,
                             Set<ModuleLicenseModel> publishedModules, Set<String> scopes) {
        this.licenses = values;
        this.publishedModules = publishedModules;
        this.scopes = scopes;
    }

    public Collection<ModuleLicenseModel> getLicenses() {
        return licenses;
    }

    public void setLicenses(Collection<ModuleLicenseModel> licenses) {
        this.licenses = licenses;
    }

    public Set<ModuleLicenseModel> getPublishedModules() {
        return publishedModules;
    }

    public void setPublishedModules(Set<ModuleLicenseModel> publishedModules) {
        this.publishedModules = publishedModules;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
