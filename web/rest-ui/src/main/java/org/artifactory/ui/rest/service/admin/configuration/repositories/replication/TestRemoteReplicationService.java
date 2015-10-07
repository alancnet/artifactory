package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
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
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.artifactory.ui.rest.service.admin.configuration.repositories.replication.TestLocalReplicationService.FAKE_CRON;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestRemoteReplicationService implements RestService<RemoteRepositoryConfigModel> {
    private static final Logger log = LoggerFactory.getLogger(TestRemoteReplicationService.class);

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    ReplicationConfigDescriptorBuilder replicationDescriptorBuilder;

    @Autowired
    RepoConfigValidator repoValidator;

    @Autowired
    RepoConfigDescriptorBuilder repoDescriptorBuilder;

    @Autowired
    ReplicationConfigValidator replicationValidator;

    @Override
    public void execute(ArtifactoryRestRequest<RemoteRepositoryConfigModel> request, RestResponse response) {
        RemoteRepositoryConfigModel model = request.getImodel();
        if (model == null) {
            response.error("No repository configuration given to test.").responseCode(SC_BAD_REQUEST);
            return;
        }
        RemoteReplicationDescriptor replication = null;
        HttpRepoDescriptor repo = null;
        try {
            setFakeCronExpForTestIfNeeded(model);
             repo = model.toDescriptor(repoValidator, repoDescriptorBuilder);
            replication = model.getReplicationDescriptor(replicationValidator, replicationDescriptorBuilder);
            if (replication == null) {
                response.error("No Replication configuration was sent to test.").responseCode(HttpStatus.SC_NOT_FOUND);
                return;
            }
            testRemoteReplication(replication, repo);
            response.info("Pull replication configuration tested successfully").responseCode(HttpStatus.SC_OK);
        } catch (Exception e) {
            log.debug("Error testing pull replication config: ", e);
            String error = "Error testing pull replication config: ";
            if (e instanceof UnknownHostException) {
                error += "\nUnknown host '";
                if (e.getMessage().equalsIgnoreCase("api")) {
                    error += repo.getUrl();
                } else {
                    error += e.getMessage();
                }
            } else {
                error += e.getMessage();
            }
            response.error(error).responseCode(SC_BAD_REQUEST);
        }
    }

    private void testRemoteReplication(RemoteReplicationDescriptor replication, RemoteRepoDescriptor repo)
            throws RepoConfigException, IOException {
        if (StringUtils.isBlank(replication.getRepoKey())) {
            replication.setRepoKey(repo.getKey());
        }
        addonsManager.addonByType(ReplicationAddon.class).validateTargetIsDifferentInstance(replication, repo);
    }

    /**
     * Sets fake cron expressions in the replication descriptors just to allow for the test to run
     * if non were set by the ui
     */
    private void setFakeCronExpForTestIfNeeded(RemoteRepositoryConfigModel model) {
        List<RemoteReplicationConfigModel> replications =
                Optional.ofNullable(model.getReplications()).orElse(Lists.newArrayList());
        replications.stream()
                .filter(replication -> StringUtils.isBlank(replication.getCronExp()))
                .forEach(emptyReplication -> emptyReplication.setCronExp(FAKE_CRON));
    }
}
