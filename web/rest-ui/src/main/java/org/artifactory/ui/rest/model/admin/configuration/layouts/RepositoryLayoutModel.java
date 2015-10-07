package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Lior Hasson
 */
public class RepositoryLayoutModel extends RepoLayout implements RestModel {
    public RepositoryLayoutModel() {}

    public RepositoryLayoutModel(RepoLayout copy) {
        super(copy);
    }

    private RepositoryAssociationsModel repositoryAssociations;

    public RepositoryAssociationsModel getRepositoryAssociations() {
        return repositoryAssociations;
    }

    public void setRepositoryAssociations(RepositoryAssociationsModel repositoryAssociations) {
        this.repositoryAssociations = repositoryAssociations;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
