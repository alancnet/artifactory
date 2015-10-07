package org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.utils.CronUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

/**
 * Service validates values in the model and sets default values as needed.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReplicationConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(ReplicationConfigValidator.class);

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    CentralConfigService configService;

    @Autowired
    RepositoryService repoService;

    /**
     * Validates the given local repo replication models and sets default values where needed and nulls are given
     *
     * @throws RepoConfigException
     */
    public void validateLocalModels(List<LocalReplicationConfigModel> replications) throws RepoConfigException {
        long enabledReplications = replications.stream()
                .filter(LocalReplicationConfigModel::isEnabled)
                .count();
        if (replications.size() > 0 && !addonsManager.isLicenseInstalled()) {
            throw new RepoConfigException("Replication is only available with a pro license and above", SC_FORBIDDEN);
        } else if (enabledReplications > 1 && !addonsManager.isHaLicensed()) {
            throw new RepoConfigException("Multi-push replication is only available with an Enterprise license.",
                    SC_FORBIDDEN);
        }
        if (replications.size() > 1) {
            checkForDuplicateUrls(replications);
        }
        for (LocalReplicationConfigModel replication : replications) {
            if (!CronUtils.isValid(replication.getCronExp())) {
                throw new RepoConfigException("Invalid cron expression", SC_BAD_REQUEST);
            }
            replication.setEnabled(Optional.of(replication.isEnabled()).orElse(false));
            //Required field, but don't fail validation for it as default is in place
            replication.setSocketTimeout(Optional.of(replication.getSocketTimeout()).orElse(15000));
            replication.setSyncDeletes(Optional.of(replication.isSyncDeletes()).orElse(false));
            replication.setSyncProperties(Optional.of(replication.isSyncProperties()).orElse(false));
            replication.setEnableEventReplication(Optional.of(replication.isEnableEventReplication()).orElse(false));
            String proxyKey = replication.getProxy();
            if ((StringUtils.isNotBlank(proxyKey)) && (configService.getDescriptor().getProxy(proxyKey) == null)) {
                throw new RepoConfigException("Invalid proxy configuration name", SC_BAD_REQUEST);
            }
            if (StringUtils.isBlank(replication.getUrl())) {
                throw new RepoConfigException("Replication url is required", SC_BAD_REQUEST);
            }
            if (StringUtils.isBlank(replication.getUsername())) {
                throw new RepoConfigException("Replication username is required", SC_BAD_REQUEST);
            }
        }
    }

    /**
     * Validates the given local repo replication models and sets default values where needed and nulls are given
     * Returns one remote Config model(because only one is allowed) or fails if the model contains more than one
     *
     * @throws RepoConfigException
     */
    public RemoteReplicationConfigModel validateRemoteModel(RemoteRepositoryConfigModel repo)
            throws RepoConfigException {

        //No config given
        List<RemoteReplicationConfigModel> replications = repo.getReplications();
        if (replications == null || replications.size() < 1 || replications.get(0) == null) {
            log.debug("No replication configuration given for repo {}", repo.getGeneral().getRepoKey());
            return null;
        }
        if (!addonsManager.isLicenseInstalled()) {
            throw new RepoConfigException("Replication is only available with a pro license", SC_FORBIDDEN);
        } else if (repo.getReplications().size() > 1) {
            throw new RepoConfigException("Only one pull replication configuration is allowed", SC_BAD_REQUEST);
        }
        RemoteReplicationConfigModel replication = replications.get(0);
        if (StringUtils.isNotBlank(replication.getCronExp()) && replication.isEnabled()
                && !CronUtils.isValid(replication.getCronExp())) {
            throw new RepoConfigException("Invalid cron expression", SC_BAD_REQUEST);
        }
        if (replication.isEnabled() &&
                (repo.getAdvanced().getNetwork() == null
                        || StringUtils.isBlank(repo.getAdvanced().getNetwork().getUsername())
                        || StringUtils.isBlank(repo.getAdvanced().getNetwork().getPassword()))) {
            throw new RepoConfigException("Pull replication requires non-anonymous authentication to the remote " +
                    "repository.\nPlease make sure to fill-in the 'Username' and 'Password' fields in the " +
                    "'Advanced Settings' tab. ", SC_UNAUTHORIZED);
        }
        replication.setEnabled(Optional.of(replication.isEnabled()).orElse(false));
        replication.setSyncDeletes(Optional.of(replication.isSyncDeletes()).orElse(false));
        replication.setSyncProperties(Optional.of(replication.isSyncProperties()).orElse(false));
        return replication;
    }

    public void validateAllTargetReplicationLicenses(LocalRepoDescriptor repo,
            List<LocalReplicationDescriptor> replications) throws RepoConfigException {
        String failMessage = "Multi Push Replication is supported for targets with an enterprise license only";
        String errorMessage = null; //is returned if something unexpected happened during tests
        int numOfActiveReplication = 0;
        int numOfTargetsFailed = 0;
        int numberOfTargetSucceeded = 0;
        int numOfReplications = replications.size();
        for (LocalReplicationDescriptor replication : replications) {
            try {
                if (!replication.isEnabled()) {
                    continue;
                }
                numOfActiveReplication++;
                addonsManager.addonByType(ReplicationAddon.class).validateTargetLicense(replication, repo,
                        numOfReplications);
                numberOfTargetSucceeded++;
            } catch (Exception error) {
                if (error.getMessage().equals(failMessage)) {
                    numOfTargetsFailed++;
                } else {
                    errorMessage = "Error occurred while testing replication config for url '" + replication.getUrl()
                            + "': " + error.getMessage();
                }
            }
        }
        if (numOfActiveReplication == numberOfTargetSucceeded) {
            log.debug("All replication targets for repo {} tested successfully", repo.getKey());
        } else if (StringUtils.isNotBlank(errorMessage)) {
            throw new RepoConfigException(errorMessage, SC_BAD_REQUEST);
        } else if (numOfActiveReplication == numOfTargetsFailed && numOfActiveReplication != 0) {
            throw new RepoConfigException(failMessage, SC_BAD_REQUEST);
        } else {
            throw new RepoConfigException("Note: " + failMessage, SC_BAD_REQUEST);
        }
    }

    private void checkForDuplicateUrls(List<LocalReplicationConfigModel> replications) throws RepoConfigException {
        Set<String> allItems = new HashSet<>(); //util set to catch duplicate urls
        Set<String> duplicates = replications.stream()
                .map(LocalReplicationConfigModel::getUrl)
                .filter(url -> !allItems.add(url))
                .collect(Collectors.toSet());
        //duplicates now contains all duplicate urls if any
        if (duplicates.size() > 0) {
            throw new RepoConfigException("Url '" + duplicates.iterator().next() + "' already exists as a " +
                    "replication target for this repository", SC_BAD_REQUEST);
        }
    }
}
