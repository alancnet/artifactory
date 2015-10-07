package org.artifactory.ui.rest.service.utils.validation;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Validations service for root entity names.
 *
 * @author Yossi Shaul
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GetNameValidatorService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getQueryParamByKey("name");
        try {
            NameValidator.validate(name);
            response.info("Name validated");
        } catch (ValidationException e) {
            response.error(e.getMessage());
        }
    }
}
