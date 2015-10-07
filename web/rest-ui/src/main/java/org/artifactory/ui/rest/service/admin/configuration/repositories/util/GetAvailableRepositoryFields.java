package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.info.RepositoryFieldsValuesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Aviad Shikloshi
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAvailableRepositoryFields implements RestService<RepositoryFieldsValuesModel> {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepositoryFieldsValuesModel fields = new RepositoryFieldsValuesModel(centralConfigService.getDescriptor(),
                repositoryService);
        response.iModel(fields);
    }
}
