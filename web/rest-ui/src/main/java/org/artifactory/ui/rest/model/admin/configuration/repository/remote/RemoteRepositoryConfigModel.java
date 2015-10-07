package org.artifactory.ui.rest.model.admin.configuration.repository.remote;

import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
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

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
@JsonTypeName("remoteRepoConfig")
public class RemoteRepositoryConfigModel implements RepositoryConfigModel<RemoteBasicRepositoryConfigModel,
        RemoteAdvancedRepositoryConfigModel, RemoteReplicationConfigModel> {

    protected GeneralRepositoryConfigModel general;
    protected RemoteBasicRepositoryConfigModel basic;
    protected RemoteAdvancedRepositoryConfigModel advanced;
    protected List<RemoteReplicationConfigModel> replications;
    protected TypeSpecificConfigModel typeSpecific;

    @Override
    public GeneralRepositoryConfigModel getGeneral() {
        return general;
    }

    @Override
    public void setGeneral(GeneralRepositoryConfigModel general) {
        this.general = general;
    }

    @Override
    public RemoteBasicRepositoryConfigModel getBasic() {
        return basic;
    }

    @Override
    public void setBasic(RemoteBasicRepositoryConfigModel basic) {
        this.basic = basic;
    }

    @Override
    public RemoteAdvancedRepositoryConfigModel getAdvanced() {
        return advanced;
    }

    @Override
    public void setAdvanced(RemoteAdvancedRepositoryConfigModel advanced) {
        this.advanced = advanced;
    }

    @Override
    public List<RemoteReplicationConfigModel> getReplications() {
        return replications;
    }

    @Override
    public void setReplications(List<RemoteReplicationConfigModel> replications) {
        this.replications = replications;
    }

    @Override
    public TypeSpecificConfigModel getTypeSpecific() {
        return typeSpecific;
    }

    @Override
    public void setTypeSpecific(TypeSpecificConfigModel typeSpecific) {
        this.typeSpecific = typeSpecific;
    }

    @Override
    public HttpRepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder)
            throws RepoConfigException {
        validator.validateRemote(this);
        return builder.buildRemoteDescriptor(this);
    }

    @Override
    public RemoteRepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor) {
        builder.populateRemoteRepositoryConfigValuesToModel((HttpRepoDescriptor) descriptor, this);
        return this;
    }

    @Override
    public void createRepo(CreateRepoConfigHelper creator) throws IOException, RepoConfigException {
        creator.handleRemote(this);
    }

    @Override
    public void updateRepo(UpdateRepoConfigHelper updater) throws IOException, RepoConfigException {
        updater.handleRemote(this);
    }

    public RemoteReplicationDescriptor getReplicationDescriptor(ReplicationConfigValidator validator,
            ReplicationConfigDescriptorBuilder builder) throws RepoConfigException {
        RemoteReplicationDescriptor descriptor = null;
        RemoteReplicationConfigModel validatedReplication = validator.validateRemoteModel(this);
        if (validatedReplication != null) {
            descriptor = builder.buildRemoteReplication(validatedReplication, general.getRepoKey());
        }
        return descriptor;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
