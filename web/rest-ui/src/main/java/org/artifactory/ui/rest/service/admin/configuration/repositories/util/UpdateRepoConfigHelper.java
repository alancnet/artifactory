package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.artifactory.addon.p2.P2Repo;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.P2TypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.replication.ReplicationConfigService;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.ReplicationConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.ReplicationConfigValidator;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Delegate helper for creating and persisting repo config
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateRepoConfigHelper {
    private static final Logger log = LoggerFactory.getLogger(UpdateRepoConfigHelper.class);

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private RepoConfigDescriptorBuilder descriptorBuilder;

    @Autowired
    private RepoConfigValidator repoConfigValidator;

    @Autowired
    private ReplicationConfigDescriptorBuilder replicationBuilder;

    @Autowired
    private ReplicationConfigService replicationConfigService;

    @Autowired
    private ReplicationConfigValidator replicationValidator;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private CreateRepoConfigHelper creator;

    public void handleLocal(LocalRepositoryConfigModel model) throws RepoConfigException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Model resolved to local repo descriptor, updating.");
        String repoKey = model.getGeneral().getRepoKey();
        LocalRepoDescriptor repoDescriptor = model.toDescriptor(repoConfigValidator, descriptorBuilder);
        Set<LocalReplicationDescriptor> replications = model.getReplicationDescriptors(replicationValidator,
                replicationBuilder);
        log.debug("Updating descriptor for {}.", repoKey);
        configDescriptor.getLocalRepositoriesMap().put(repoDescriptor.getKey(), repoDescriptor);
        replicationConfigService.updateLocalReplications(replications, repoKey, configDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleRemote(RemoteRepositoryConfigModel model) throws IOException, RepoConfigException {
        log.debug("Model resolved to remote repo descriptor, updating.");
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        String repoKey = model.getGeneral().getRepoKey();
        HttpRepoDescriptor repoDescriptor = model.toDescriptor(repoConfigValidator, descriptorBuilder);
        RemoteReplicationDescriptor replication = model.getReplicationDescriptor(replicationValidator,
                replicationBuilder);
        log.debug("Updating descriptor for {}.", repoDescriptor.getKey());
        if (replication != null) {
            replicationConfigService.updateRemoteReplication(replication, repoDescriptor, configDescriptor);
        }

        configDescriptor.getRemoteRepositoriesMap().put(repoKey, repoDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleVirtual(VirtualRepositoryConfigModel model) throws RepoConfigException {
        log.debug("Model resolved to virtual repo descriptor, updating.");
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        String repoKey = model.getGeneral().getRepoKey();
        VirtualRepoDescriptor repoDescriptor = model.toDescriptor(repoConfigValidator, descriptorBuilder);
        if (repoDescriptor.getType().equals(RepoType.P2)) {
            updateP2Config(configDescriptor, repoDescriptor,
                    ((P2TypeSpecificConfigModel) model.getTypeSpecific()).getP2Repos());
        }
        log.debug("Updating descriptor for {}.", repoDescriptor.getKey());
        configDescriptor.getVirtualRepositoriesMap().put(repoKey, repoDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    private void updateP2Config(MutableCentralConfigDescriptor configDescriptor, VirtualRepoDescriptor repoDescriptor,
            List<P2Repo> requestedRepos) {
        log.debug("Updating P2 config for repo {}", repoDescriptor.getKey());
        //Preserve all already-aggregated virtual repos this repo might have for backwards compatibility although
        // we don't allow adding new ones anymore, and append to the ones returned by the P2 config logic
        List<RepoDescriptor> reposToAdd = repoService.virtualRepoDescriptorByKey(repoDescriptor.getKey())
                .getRepositories().stream()
                .filter(aggregatedRepo -> !aggregatedRepo.isReal()).collect(Collectors.toList());
        reposToAdd.addAll(creator.validateAndCreateP2ConfigRepos(configDescriptor, repoDescriptor, requestedRepos));
        repoDescriptor.setRepositories(reposToAdd);
    }
}
