package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExecuteAllLocalReplicationsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ExecuteAllLocalReplicationsService.class);

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        LocalRepoDescriptor repo = repoService.localRepoDescriptorByKey(repoKey);
        if (repo == null) {
            response.error("Repository '" + repoKey + "' doesn't exist, the 'run now' button only works for exiting " +
                    "repositories").responseCode(SC_NOT_FOUND);
            return;
        }

        log.info("Scheduling all enabled push replications tasks for repository {}", repoKey);
        List<LocalReplicationDescriptor> enabledReplications = configService.getDescriptor()
                .getMultiLocalReplications(repoKey)
                .stream().filter(LocalReplicationDescriptor::isEnabled).collect(Collectors.toList());
        if(CollectionUtils.isNullOrEmpty(enabledReplications)) {
            String warn = "No active push replications are configured for";
            log.warn(warn + " repo '{}'.", repoKey);
            response.warn(warn + "this repo.");
            return;
        }
        boolean moreThenOneActiveAndNotHa = false;
        String moreThenOneActiveAndNotHaError = "Only an enterprise license can run a multi-push replication setup. " +
                "Only the first enabled replication";
        if(!addonsManager.isHaLicensed() && enabledReplications.size() > 1) {
            moreThenOneActiveAndNotHa = true;
            log.warn(moreThenOneActiveAndNotHaError + " will run.");
            LocalReplicationDescriptor firstEnabled = enabledReplications.get(0);
            enabledReplications.clear();
            enabledReplications.add(firstEnabled);
        }

        boolean hadErrors = false;
        for (LocalReplicationDescriptor replication : enabledReplications) {
            log.info("Scheduling push replication task for repo {} and url {}", repoKey, replication.getUrl());
            BasicStatusHolder status = new BasicStatusHolder();
            ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
            try {
                replicationAddon.validateTargetLicense(replication, repo, enabledReplications.size());
                replicationAddon.scheduleImmediateLocalReplicationTask(replication, status);
            } catch (Exception e) {
                String error = "Error scheduling push replication task: ";
                if (e instanceof UnknownHostException) {
                    error += "\nUnknown host: ";
                    if (e.getMessage().equalsIgnoreCase("api")) {
                        error += replication.getUrl();
                    } else {
                        error += e.getMessage();
                    }
                } else {
                    error += e.getMessage();
                }
                hadErrors = true;
                log.debug(error, e);
            }
            if (status.hasErrors()) {
                hadErrors = true;
                log.error(status.getLastError().getMessage(), status.getLastError().getException());
            } else if (!hadErrors) {
                log.debug("Replication task for {} was successfully scheduled to run in the background",
                        replication.getUrl());
            }
        }
        if (hadErrors) {
            response.error("Replication tasks scheduling finished with errors. Check Artifactory logs for more details.");
        } else if (moreThenOneActiveAndNotHa) {
            response.warn(moreThenOneActiveAndNotHaError + " was successfully scheduled to run");
        } else {
            response.info("Replication tasks scheduling finished successfully");
        }
    }
}
