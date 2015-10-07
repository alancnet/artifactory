package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExecuteRemoteReplicationService<T extends RepositoryConfigModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(ExecuteRemoteReplicationService.class);

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    CentralConfigService configService;

    @Autowired
    RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = RequestUtils.getRepoKeyFromRequest(request);
        RemoteReplicationDescriptor replicationDesc = configService.getDescriptor().getRemoteReplication(repoKey);
        if (replicationDesc == null) {
            response.error("Repository does not exists");
            return;
        }
        BasicStatusHolder status = new BasicStatusHolder();
        try {
            runPullReplication(repoKey, status);
            if (status.hasErrors()) {
                log.debug(status.getLastError().getMessage(), status.getLastError().getException());
                response.error("Error scheduling replication task: "
                        + status.getLastError().getMessage()).responseCode(SC_BAD_REQUEST);
            } else {
                response.info("Replication task was successfully scheduled to run in the background.");
            }
        } catch (Exception e) {
            response.error(e.getMessage()).responseCode(SC_BAD_REQUEST);
        }
    }

    private void runPullReplication(String repoKey, BasicStatusHolder status) throws RepoConfigException, IOException {
        log.debug("Model resolved to remote replication, retrieving replication descriptor.");
        if (!addonsManager.isLicenseInstalled()) {
            throw new RepoConfigException("Replication is only available with a pro license and above", SC_FORBIDDEN);
        }
        RemoteRepoDescriptor remoteRepo = repoService.remoteRepoDescriptorByKey(repoKey);
        if (remoteRepo == null) {
            throw new RepoConfigException("Repository '" + repoKey + "' doesn't exist, the 'run now' button only " +
                    "works for exiting repositories", SC_NOT_FOUND);
        }
        RemoteReplicationDescriptor replication = configService.getDescriptor().getRemoteReplication(repoKey);
        if (replication == null) {
            throw new RepoConfigException("Replication configuration for repository '" + repoKey + "' doesn't exist, " +
                    "the 'run now' button only works for exiting replication configurations - save the configuration and"
                    + " try again.", SC_NOT_FOUND);
        }
        log.info("Scheduling remote replication task for repository {}", repoKey);
        addonsManager.addonByType(ReplicationAddon.class).scheduleImmediateRemoteReplicationTask(replication, status);
    }
}