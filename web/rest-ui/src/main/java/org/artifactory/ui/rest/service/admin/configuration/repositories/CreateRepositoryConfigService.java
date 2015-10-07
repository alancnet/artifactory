package org.artifactory.ui.rest.service.admin.configuration.repositories;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateRepositoryConfigService implements RestService<RepositoryConfigModel> {
    private static final Logger log = LoggerFactory.getLogger(CreateRepositoryConfigService.class);

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private RepoConfigValidator repoValidator;

    @Autowired
    private CreateRepoConfigHelper creator;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepositoryConfigModel model = (RepositoryConfigModel) request.getImodel();
        createRepo(response, model);
    }

    public boolean createRepo(RestResponse response, RepositoryConfigModel model) {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        String repoKey = model.getGeneral().getRepoKey();
        if (configDescriptor.isRepositoryExists(repoKey)) {
            response.error("Repository " + repoKey + " already exists")
                    .responseCode(HttpStatus.SC_BAD_REQUEST);
            return false;
        }
        log.info("Creating repository {}", repoKey);
        try {
            //Run repo name validation only on create
            repoValidator.validateRepoName(model.getGeneral().getRepoKey());
            model.createRepo(creator);
            response.info("Successfully added repository '" + repoKey + "'");
        } catch (Exception e) {
            log.error("Failed to create repository {}: {}", repoKey, e.getMessage());
            log.debug("Failed to create repository: ", e);
            response.error("Failed to create repository " + repoKey + ": " + e.getMessage());
            return false;
        }
        return true;
    }
}
