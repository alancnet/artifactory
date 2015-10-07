package org.artifactory.ui.rest.model.admin.configuration.repository.local;

import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.ReplicationConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.ReplicationConfigValidator;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeName("localRepoConfig")
public class LocalRepositoryConfigModel implements RepositoryConfigModel<LocalBasicRepositoryConfigModel,
        LocalAdvancedRepositoryConfigModel, LocalReplicationConfigModel> {

    protected GeneralRepositoryConfigModel general;
    protected LocalBasicRepositoryConfigModel basic;
    protected LocalAdvancedRepositoryConfigModel advanced;
    private List<LocalReplicationConfigModel> replications;
    protected TypeSpecificConfigModel typeSpecific;

    @Override
    public GeneralRepositoryConfigModel getGeneral() {
        return general;
    }

    public void setGeneral(GeneralRepositoryConfigModel general) {
        this.general = general;
    }

    @Override
    public LocalBasicRepositoryConfigModel getBasic() {
        return basic;
    }

    @Override
    public void setBasic(LocalBasicRepositoryConfigModel basic) {
        this.basic = basic;
    }

    @Override
    public LocalAdvancedRepositoryConfigModel getAdvanced() {
        return advanced;
    }

    @Override
    public void setAdvanced(LocalAdvancedRepositoryConfigModel advanced) {
        this.advanced = advanced;
    }

    @Override
    public List<LocalReplicationConfigModel> getReplications() {
        return replications;
    }

    @Override
    public void setReplications(List<LocalReplicationConfigModel> replications) {
        this.replications = replications;
    }

    @Override
    public TypeSpecificConfigModel getTypeSpecific() {
        return typeSpecific;
    }

    public void setTypeSpecific(TypeSpecificConfigModel typeSpecific) {
        this.typeSpecific = typeSpecific;
    }

    @Override
    public LocalRepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder)
            throws RepoConfigException {
        validator.validateLocal(this);
        return builder.buildLocalDescriptor(this);
    }

    @Override
    public LocalRepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor) {
        builder.populateLocalDescriptorValuesToModel((LocalRepoDescriptor) descriptor, this);
        return this;
    }

    public Set<LocalReplicationDescriptor> getReplicationDescriptors(ReplicationConfigValidator validator,
            ReplicationConfigDescriptorBuilder builder) throws RepoConfigException {
        validator.validateLocalModels(replications);
        return builder.buildLocalReplications(replications, general.getRepoKey());
    }

    @Override
    public void createRepo(CreateRepoConfigHelper creator) throws IOException, RepoConfigException {
        creator.handleLocal(this);
    }

    @Override
    public void updateRepo(UpdateRepoConfigHelper updater) throws IOException, RepoConfigException {
        updater.handleLocal(this);
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
