package org.artifactory.ui.rest.service.admin.configuration.repositories;

import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
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
public class DeleteRepositoryConfigService<T extends RepositoryConfigModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(DeleteRepositoryConfigService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        String repoKey = request.getPathParamByKey("repoKey");
        if (!configDescriptor.isRepositoryExists(repoKey)) {
            response.error("Repository '" + repoKey + "' does not exist").responseCode(HttpStatus.SC_NOT_FOUND);
        } else if (configDescriptor.getLocalRepositoriesMap().keySet().contains(repoKey)
                && configDescriptor.getLocalRepositoriesMap().size() == 1) {
            //Don't allow deleting the last local repo
            response.error("Deleting the last local repository is not allowed").responseCode(HttpStatus.SC_FORBIDDEN);
        } else {
            try {
                log.info("Deleting repository {}", repoKey);
                configDescriptor.removeRepository(repoKey);
                centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
                response.info("Successfully deleted '" + repoKey + "' repository")
                        .responseCode(HttpStatus.SC_OK);
            } catch (Exception e) {
                log.debug("Descriptor save failed: ", e);
                log.error("Deleting repo '{}' failed: {}", repoKey, e.getMessage());
                response.error("Deleting repo '" + repoKey + "' failed: " + e.getMessage());
            }
        }
    }
}
