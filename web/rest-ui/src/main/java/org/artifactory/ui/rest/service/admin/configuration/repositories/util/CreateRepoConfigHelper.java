package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.p2.P2Addon;
import org.artifactory.addon.p2.P2Repo;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Delegate helper for creating and persisting repo config
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateRepoConfigHelper {
    private static final Logger log = LoggerFactory.getLogger(CreateRepoConfigHelper.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepoConfigDescriptorBuilder repoBuilder;

    @Autowired
    private ReplicationConfigDescriptorBuilder replicationBuilder;

    @Autowired
    private ReplicationConfigService replicationConfigService;

    @Autowired
    private ReplicationConfigValidator replicationValidator;

    @Autowired
    private RepoConfigValidator repoValidator;

    @Autowired
    private CentralConfigService configService;


    public void handleLocal(LocalRepositoryConfigModel model) throws RepoConfigException, IOException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Model resolved to local repo descriptor, adding.");
        log.debug("Creating descriptor from received model");
        LocalRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder);
        configDescriptor.addLocalRepository(repoDescriptor);
        if (model.getReplications() != null) {
            log.debug("Creating push replication descriptor(s) from received model");
            Set<LocalReplicationDescriptor> replications = model.getReplicationDescriptors(replicationValidator,
                    replicationBuilder);
            replicationConfigService.addLocalReplications(replications, repoDescriptor, configDescriptor);
        }
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleRemote(RemoteRepositoryConfigModel model) throws RepoConfigException, IOException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Model resolved to remote repo descriptor, adding.");
        log.debug("Creating descriptor from received model");
        HttpRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder);
        configDescriptor.addRemoteRepository(repoDescriptor);
        if (model.getReplications() != null) {
            log.debug("Creating pull replication descriptor from received model");
            RemoteReplicationDescriptor replicationDescriptor = model.getReplicationDescriptor(replicationValidator,
                    replicationBuilder);
            replicationConfigService.addRemoteReplication(replicationDescriptor, repoDescriptor, configDescriptor);
        }
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public void handleVirtual(VirtualRepositoryConfigModel model) throws RepoConfigException {
        log.debug("Model resolved to virtual repo descriptor, adding.");
        log.debug("Creating descriptor from received model");
        VirtualRepoDescriptor repoDescriptor = model.toDescriptor(repoValidator, repoBuilder);
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        if (repoDescriptor.getType().equals(RepoType.P2)) {
            log.debug("Creating P2 config for repo {}", repoDescriptor.getKey());
            repoDescriptor.setRepositories(validateAndCreateP2ConfigRepos(configDescriptor, repoDescriptor,
                    ((P2TypeSpecificConfigModel) model.getTypeSpecific()).getP2Repos()));
        }
        configDescriptor.addVirtualRepository(repoDescriptor);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    public List<RepoDescriptor> validateAndCreateP2ConfigRepos(MutableCentralConfigDescriptor configDescriptor,
            VirtualRepoDescriptor repoDescriptor, List<P2Repo> requestedRepos) {
        BasicStatusHolder status = new BasicStatusHolder();
        Map<String, List<String>> subCompositeUrls = Maps.newHashMap();
        //Addon verifies each url and crunches out a list of local and remote repositories that should be aggregated
        //In this virtual based on the URLs passed to it
        List<P2Repo> p2Repos = addonsManager.addonByType(P2Addon.class).verifyRemoteRepositories(configDescriptor,
                repoDescriptor, null, requestedRepos, subCompositeUrls, status);
        List<RepoDescriptor> descriptorsToAdd = Lists.newArrayList();
        for (P2Repo repo : p2Repos) {
            RepoDescriptor newDescriptor = repo.getDescriptor();
            if (!configDescriptor.isRepositoryExists(repo.getRepoKey())) {
                if (repo.getDescriptor() instanceof HttpRepoDescriptor) {
                    HttpRepoDescriptor newRemoteDescriptor = (HttpRepoDescriptor) newDescriptor;
                    //Set default proxy for automatically created new remote repos
                    newRemoteDescriptor.setProxy(configDescriptor.defaultProxyDefined());
                    //P2 remote repos list remote items by default, as we're not creating through the normal
                    //model chain we have to set it here for new repos
                    newRemoteDescriptor.setListRemoteFolderItems(
                            RepoConfigDefaultValues.DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE);
                    configDescriptor.addRemoteRepository(newRemoteDescriptor);
                }
                log.info("Remote repository {} is being created based on {}'s P2 config", newDescriptor.getKey(),
                        repoDescriptor.getKey());
            }
            descriptorsToAdd.add(newDescriptor);
        }
        return descriptorsToAdd.stream().distinct().collect(Collectors.toList());
    }
}
