package org.artifactory.ui.rest.model.admin.configuration.repository.virtual;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigModelBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.io.IOException;
import java.util.List;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeName("virtualRepoConfig")
public class VirtualRepositoryConfigModel implements RepositoryConfigModel<VirtualBasicRepositoryConfigModel,
        VirtualAdvancedRepositoryConfigModel, RepositoryReplicationConfigModel> {

    private GeneralRepositoryConfigModel general;
    private VirtualBasicRepositoryConfigModel basic;
    private VirtualAdvancedRepositoryConfigModel advanced;
    private TypeSpecificConfigModel typeSpecific;

    @Override
    public VirtualBasicRepositoryConfigModel getBasic() {
        return basic;
    }

    @Override
    public void setBasic(VirtualBasicRepositoryConfigModel basic) {
        this.basic = basic;
    }

    @Override
    public VirtualAdvancedRepositoryConfigModel getAdvanced() {
        return advanced;
    }

    @Override
    public void setAdvanced(VirtualAdvancedRepositoryConfigModel advanced) {
        this.advanced = advanced;
    }

    @Override
    public GeneralRepositoryConfigModel getGeneral() {
        return general;
    }

    @Override
    public void setGeneral(GeneralRepositoryConfigModel general) {
        this.general = general;
    }

    @Override
    public List<RepositoryReplicationConfigModel> getReplications() {
        return null;
    }

    @Override
    public void setReplications(List<RepositoryReplicationConfigModel> replication) {

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
    public VirtualRepoDescriptor toDescriptor(RepoConfigValidator validator, RepoConfigDescriptorBuilder builder)
            throws RepoConfigException {
        validator.validateVirtual(this);
        return builder.buildVirtualDescriptor(this);
    }

    @Override
    public VirtualRepositoryConfigModel fromDescriptor(RepoConfigModelBuilder builder, RepoDescriptor descriptor) {
        builder.populateVirtualRepositoryConfigValuesToModel((VirtualRepoDescriptor) descriptor, this);
        return this;
    }

    @Override
    public void createRepo(CreateRepoConfigHelper creator) throws IOException, RepoConfigException {
        creator.handleVirtual(this);
    }

    @Override
    public void updateRepo(UpdateRepoConfigHelper updater) throws IOException, RepoConfigException {
        updater.handleVirtual(this);
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
