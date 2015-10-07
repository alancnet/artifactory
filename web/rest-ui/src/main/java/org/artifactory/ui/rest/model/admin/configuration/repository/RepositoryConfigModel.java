package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.io.IOException;
import java.util.List;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
// TODO: [by dan] we need visible = true (Jackson 2) to see the type property, so we are forced to use instanceof
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocalRepositoryConfigModel.class, name = "localRepoConfig"),
        @JsonSubTypes.Type(value = RemoteRepositoryConfigModel.class, name = "remoteRepoConfig"),
        @JsonSubTypes.Type(value = VirtualRepositoryConfigModel.class, name = "virtualRepoConfig")
})
public interface RepositoryConfigModel<B extends BasicRepositoryConfigModel, A extends AdvancedRepositoryConfigModel,
        R extends RepositoryReplicationConfigModel> extends RestModel {

    GeneralRepositoryConfigModel getGeneral();

    void setGeneral(GeneralRepositoryConfigModel general);

    B getBasic();

    void setBasic(B basic);

    A getAdvanced();

    void setAdvanced(A advanced);

    List<R> getReplications();

    void setReplications(List<R> replication);

    TypeSpecificConfigModel getTypeSpecific();

    void setTypeSpecific(TypeSpecificConfigModel typeSpecific);

    /**
     * Uses the builder to produce a descriptor from this model
     */
    @JsonIgnore
    RepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder)
            throws RepoConfigException;

    @JsonIgnore
    RepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor);

    @JsonIgnore
    void createRepo(CreateRepoConfigHelper creator) throws IOException, RepoConfigException;

    @JsonIgnore
    void updateRepo(UpdateRepoConfigHelper updater) throws IOException, RepoConfigException;
}
