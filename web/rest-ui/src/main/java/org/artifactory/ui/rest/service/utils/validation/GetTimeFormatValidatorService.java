package org.artifactory.ui.rest.service.utils.validation;

import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * Validates input according to {@link SimpleDateFormat} time format.
 *
 * @author Yossi Shaul
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GetTimeFormatValidatorService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String dateFormat = request.getQueryParamByKey("dateformat");
        if (StringUtils.isEmpty(dateFormat)) {
            response.error("Please enter a valid date format");
        } else {
            try {
                new SimpleDateFormat(dateFormat);
                response.info("Date format validated");
            } catch (IllegalArgumentException e) {
                response.error("Please enter a valid date format");
            }
        }
    }
}
