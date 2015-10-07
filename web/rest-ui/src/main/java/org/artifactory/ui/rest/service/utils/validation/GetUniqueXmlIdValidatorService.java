package org.artifactory.ui.rest.service.utils.validation;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Validations service for unique central config xml name.
 *
 * @author Yossi Shaul
 * @see UniqueXmlIdValidator
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GetUniqueXmlIdValidatorService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String id = request.getQueryParamByKey("id");
        try {
            new UniqueXmlIdValidator(centralConfigService.getMutableDescriptor()).validate(id);
            response.info("Unique id validated");
        } catch (ValidationException e) {
            response.error(e.getMessage());
        }
    }
}
