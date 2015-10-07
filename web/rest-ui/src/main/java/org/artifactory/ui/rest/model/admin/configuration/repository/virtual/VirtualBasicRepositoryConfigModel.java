package org.artifactory.ui.rest.model.admin.configuration.repository.virtual;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;

import java.util.List;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class VirtualBasicRepositoryConfigModel extends LocalBasicRepositoryConfigModel {

    private List<VirtualSelectedRepository> selectedRepositories;
    private List<String> resolvedRepositories;

    public List<VirtualSelectedRepository> getSelectedRepositories() {
        return selectedRepositories;
    }

    public void setSelectedRepositories(
            List<VirtualSelectedRepository> selectedRepositories) {
        this.selectedRepositories = selectedRepositories;
    }

    public List<String> getResolvedRepositories() {
        return resolvedRepositories;
    }

    public void setResolvedRepositories(List<String> resolvedRepositories) {
        this.resolvedRepositories = resolvedRepositories;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
