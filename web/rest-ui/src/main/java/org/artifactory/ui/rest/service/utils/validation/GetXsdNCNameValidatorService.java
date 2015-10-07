package org.artifactory.ui.rest.service.utils.validation;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.jdom2.Verifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Checks if a string is a valid xsd <a href="http://www.w3.org/TR/REC-xml-names/#NT-NCName"/>NCName</a> string.
 *
 * @author Yossi Shaul
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class GetXsdNCNameValidatorService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getQueryParamByKey("xmlName");
        String result = Verifier.checkXMLName(name);
        if (result != null) {
            response.error("Invalid XML name");
        } else {
            // successful validation
            response.info("XML name validated");
        }
    }
}
