package org.artifactory.ui.rest.service.admin.configuration.repositories;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.UpdateRepoConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateRepositoryConfigService implements RestService<RepositoryConfigModel> {
    private static final Logger log = LoggerFactory.getLogger(UpdateRepositoryConfigService.class);

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private UpdateRepoConfigHelper updater;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepositoryConfigModel model = (RepositoryConfigModel) request.getImodel();
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        log.debug("Creating descriptor from received model");
        String repoKey = model.getGeneral().getRepoKey();
        if (!configDescriptor.isRepositoryExists(repoKey)) {
            response.error("Repository '" + repoKey + "' doesn't exist.").responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        log.info("Updating repository {}", repoKey);
        try {
            model.updateRepo(updater);
            response.info("Successfully updated repository '" + repoKey + "'");
        } catch (Exception e) {
            log.error("Failed to update repository {}: {}", repoKey, e.getMessage());
            log.debug("Failed to update repository: ", e);
            response.error("Failed to update repository '" + repoKey + "'");
        }
    }
}
