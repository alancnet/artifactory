package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.ReplicationConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder.RepoConfigDescriptorBuilder;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.ReplicationConfigValidator;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.artifactory.util.stream.BiOptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestLocalReplicationService implements RestService<LocalRepositoryConfigModel> {
    public static final String REPLICATION_URL_PARAM = "replicationUrl";
    public static final String FAKE_CRON = "0 0 12 1/1 * ? *";
    private static final Logger log = LoggerFactory.getLogger(TestLocalReplicationService.class);

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
    public void execute(ArtifactoryRestRequest<LocalRepositoryConfigModel> request, RestResponse response) {
        String replicationUrl = request.getQueryParamByKey(REPLICATION_URL_PARAM);
        LocalRepositoryConfigModel model = request.getImodel();
        if (model == null) {
            response.error("No repository configuration given to test replication with.").responseCode(SC_BAD_REQUEST);
            return;
        } else if (StringUtils.isBlank(replicationUrl)) {
            response.error("No url given to identify which replication target to test").responseCode(SC_BAD_REQUEST);
            return;
        }
        try {
            setFakeCronExpForTestIfNeeded(model);
            LocalRepoDescriptor repo = model.toDescriptor(repoValidator, repoDescriptorBuilder);
            Set<LocalReplicationDescriptor> replications = model.getReplicationDescriptors(replicationValidator,
                    replicationDescriptorBuilder);
            int activeReplications = (int) replications.stream()
                    .filter(LocalReplicationDescriptor::isEnabled)
                    .count();
            BiOptional.of(replications.stream()
                    .filter(replication -> replicationUrl.equals(replication.getUrl()))
                    .findFirst())
                    .ifNotPresent(() -> response.error("No replication configuration exists for this repo  and url '"
                            + replicationUrl + "'"))
                    .ifPresent(replication -> testReplicationTarget(repo, replication, response, activeReplications));
        } catch (RepoConfigException rce) {
            log.debug("Error testing local replication: ", rce);
            response.error(rce.getMessage()).responseCode(rce.getStatusCode());
        }
    }

    private void testReplicationTarget(LocalRepoDescriptor repoDescriptor, LocalReplicationDescriptor replication,
            RestResponse response, int totalActiveReplications) {
        if (StringUtils.isBlank(replication.getRepoKey())) {
            replication.setRepoKey(repoDescriptor.getKey());
        }
        try {
            addonsManager.addonByType(ReplicationAddon.class).validateTargetLicense(replication, repoDescriptor,
                    totalActiveReplications);
            response.info("Push replication target url '" + replication.getUrl() + "' tested successfully")
                    .responseCode(HttpStatus.SC_OK);
        } catch (Exception e) {
            log.debug("Error testing push replication config: ", e);
            String error = "Error testing pull replication config: ";
            if (e instanceof UnknownHostException) {
                error += "unknown host '";
                if (e.getMessage().equalsIgnoreCase("api")) {
                    error += replication.getUrl();
                } else {
                error += e.getMessage();
                }
            } else {
                error += e.getMessage();
            }
            response.error(error).responseCode(SC_BAD_REQUEST);
        }
    }

    /**
     * Sets fake cron expressions in the replication descriptors just to allow for the test to run
     * if non were set by the ui
     */
    private void setFakeCronExpForTestIfNeeded(LocalRepositoryConfigModel model) {
        List<LocalReplicationConfigModel> replications =
                Optional.ofNullable(model.getReplications()).orElse(Lists.newArrayList());
        replications.stream()
                .filter(replication -> StringUtils.isBlank(replication.getCronExp()))
                .forEach(emptyReplication -> emptyReplication.setCronExp(FAKE_CRON));
    }
}