package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.ReplicationConfigValidator;
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

import static org.apache.http.HttpStatus.SC_FORBIDDEN;

/**
 * Service to handle replication specific operations for the repo config services.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReplicationConfigService {
    public static final String IS_REPLICATION_QUERY_PARAM = "isReplication";
    private static final Logger log = LoggerFactory.getLogger(ReplicationConfigService.class);
    @Autowired
    AddonsManager addonsManager;

    @Autowired
    CentralConfigService configService;

    @Autowired
    ReplicationConfigValidator validator;


    public void addLocalReplications(Set<LocalReplicationDescriptor> replications, LocalRepoDescriptor repo,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException, IOException {
        String repoKey = repo.getKey();
        if (replications.size() == 0) {
            log.debug("No replication config received for repo {} creation", repoKey);
        } else if (replications.size() > 1) {   //multi-push
            addMultiPushReplications(replications, repo, configDescriptor);
        } else {
            log.info("Adding push replication config for repo {}", repoKey);
            LocalReplicationDescriptor replication = replications.iterator().next();
            configDescriptor.addLocalReplication(replication);
        }
    }

    private void addMultiPushReplications(Set<LocalReplicationDescriptor> replications, LocalRepoDescriptor repo,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        if (!addonsManager.isHaLicensed()) {
            throw new RepoConfigException(
                    "Multi-push replication is only available with an Enterprise license.", SC_FORBIDDEN);
        }
        String repoKey = repo.getKey();
        validator.validateAllTargetReplicationLicenses(repo, Lists.newArrayList(replications));
        log.info("Adding multi-push replication configurations for repo {}", repoKey);
        replications.forEach(configDescriptor::addLocalReplication);
    }

    public void addRemoteReplication(RemoteReplicationDescriptor replication, RemoteRepoDescriptor repo,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException, IOException {
        log.info("Adding pull replication config for repo {}", repo.getKey());
        configDescriptor.addRemoteReplication(replication);
    }

    public void updateLocalReplications(Set<LocalReplicationDescriptor> replications, String repoKey,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        if (replications.size() == 0) {
            log.debug("No replication config received for repo {}, deleting existing ones", repoKey);
            List<LocalReplicationDescriptor> currentReplications = configDescriptor.getMultiLocalReplications(repoKey);
            cleanupLocalReplications(currentReplications);
            currentReplications.forEach(configDescriptor::removeLocalReplication);
            updateAddedAndRemovedReplications(replications, repoKey, configDescriptor);
        } else {
            LocalReplicationDescriptor replicationDescriptor = replications.iterator().next();
            setRepoKey(replicationDescriptor, repoKey);
            updateAddedAndRemovedReplications(replications, repoKey, configDescriptor);
        }
    }

    public void updateRemoteReplication(RemoteReplicationDescriptor replication, RemoteRepoDescriptor repo,
            MutableCentralConfigDescriptor configDescriptor) throws IOException {
        String repoKey = repo.getKey();
        log.info("Updating remote replication config for repo {}", repoKey);
        configDescriptor.removeRemoteReplication(configDescriptor.getRemoteReplication(repoKey));
        configDescriptor.addRemoteReplication(replication);
    }

    private void updateAddedAndRemovedReplications(Set<LocalReplicationDescriptor> replications, String repoKey,
            MutableCentralConfigDescriptor configDescriptor) {
        log.info("Updating replication configurations for repo {}", repoKey);
        // Remove all replication configs for this repo and re-add all newly received ones and do cleanup for
        // descriptors that had their url changed
        Set<String> newUrls = replications.stream()
                .map(LocalReplicationDescriptor::getUrl)
                .collect(Collectors.toSet());
        List<LocalReplicationDescriptor> currentLocalReplications = configDescriptor.getMultiLocalReplications(repoKey);
        cleanupLocalReplications(currentLocalReplications.stream()
                .filter(replication -> !newUrls.contains(replication.getUrl()))
                .collect(Collectors.toList()));
        currentLocalReplications.forEach(configDescriptor::removeLocalReplication);
        replications.forEach(configDescriptor::addLocalReplication);
    }

    /**
     * Cleans up properties left over from a local replication in the relevant repo.
     * Use only when deleting repos and when a url changes in a replication config.
     */
    public void cleanupLocalReplications(List<LocalReplicationDescriptor> toRemove) {
        toRemove.forEach(addonsManager.addonByType(ReplicationAddon.class)::cleanupLocalReplicationProperties);
    }

    private void setRepoKey(ReplicationBaseDescriptor replication, String repoKey) {
        if (StringUtils.isBlank(replication.getRepoKey())) {
            replication.setRepoKey(repoKey);
        }
    }
}
