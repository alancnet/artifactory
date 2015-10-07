package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import org.apache.commons.lang.StringUtils;
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
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.ReplicationConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.util.stream.BiOptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExecuteLocalReplicationService<T extends LocalRepositoryConfigModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(ExecuteLocalReplicationService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private ReplicationConfigDescriptorBuilder builder;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        LocalRepositoryConfigModel model = request.getImodel();
        GeneralRepositoryConfigModel general = model.getGeneral();
        if (general == null || general.getRepoKey() == null) {
            response.error("Repository key is not configured.");
            return;
        }
        String repoKey = general.getRepoKey();
        String remoteUrl = request.getQueryParamByKey("replicationUrl");
        List<LocalReplicationConfigModel> localReplicationDescriptorList = model.getReplications();
        Optional<LocalReplicationConfigModel> optionalDesc = localReplicationDescriptorList.stream().filter(
                replicationModel -> StringUtils.equals(remoteUrl, replicationModel.getUrl())).findFirst();

        BiOptional.of(optionalDesc).ifPresent(replication -> {
            try {
                boolean success = runSinglePushReplication(repoKey, remoteUrl, replication, new BasicStatusHolder());
                if (success) {
                    response.info("The replication tasks was successfully scheduled to run");
                    log.info("Replication task for repo {} and url {} successfully scheduled to run", repoKey, remoteUrl);
                } else {
                    response.error("Replication tasks scheduling finished with errors. Check Artifactory logs for more "
                                    + "details.").responseCode(SC_BAD_REQUEST);
                }
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
                response.error(error).responseCode(SC_BAD_REQUEST);
                log.error(error);
            }
        }).ifNotPresent(() -> response.error("Could not find replication."));
    }

    private boolean runSinglePushReplication(String repoKey, String url, LocalReplicationConfigModel replicationModel,
            BasicStatusHolder status) throws RepoConfigException, IOException {
        log.debug("Model resolved to local, retrieving replication descriptor.");
        log.info("immediate schedule of push replication task for repo {} and url {} was requested.", repoKey, url);
        LocalRepoDescriptor localRepo = repoService.localRepoDescriptorByKey(repoKey);
        if (localRepo == null) {
            log.debug("No such repo {}", repoKey);
            throw new RepoConfigException("Repository '" + repoKey + "' doesn't exist, the 'run now' function only " +
                    "works for exiting repositories", SC_NOT_FOUND);
        }
        LocalReplicationDescriptor replication = builder.buildLocalReplication(replicationModel, repoKey);
        if (replication == null) {
            log.warn("No such replication config for repo {} and url {}", repoKey, url);
            throw new RepoConfigException("Replication config for repository '" + repoKey + "' and url '" + url +
                    "' doesn't exist, the 'run now' button only works for exiting replication configurations - " +
                    "save the configuration and try again.", SC_NOT_FOUND);
        }
        int numberOfReplications = configService.getDescriptor().getTotalNumOfActiveLocalReplication(repoKey);
        return scheduleSinglePushReplication(repoKey, url, status, localRepo, replication, numberOfReplications);
    }

    public boolean scheduleSinglePushReplication(String repoKey, String url, BasicStatusHolder status,
            LocalRepoDescriptor repo, LocalReplicationDescriptor replication, int numOfReplications) throws IOException {
        ReplicationAddon replicationAddon = addonsManager.addonByType(ReplicationAddon.class);
        log.info("Scheduling remote replication task for repository {}", repoKey);
        replicationAddon.validateTargetLicense(replication, repo, numOfReplications);
        replicationAddon.scheduleImmediateLocalReplicationTask(replication, status);
        if(status.hasErrors()) {
            status.getErrors().forEach(error -> {
                log.error("Error scheduling push replication for repo {} and url {}: {}", repoKey, url,
                        error.getMessage());
                if (error.getException() != null) {
                    log.debug("Error scheduling push replication for repo {} and url {}: {} -> {}", repoKey, url,
                            error.getMessage(), error.getException());
                }
            });
            return false;
        }
        return true;
    }
}
