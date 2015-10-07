package org.artifactory.ui.rest.service.admin.advanced.securitydescriptor;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.SecurityService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.advanced.securitydescriptor.SecurityDescriptorModel;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateSecurityDescriptorService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateSecurityDescriptorService.class);

    @Autowired
    private SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateSecurityDescriptor");
        //update security xml
        updateSecurityXml(request, response);
    }

    /**
     * save updated security xml and update response feedback
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void updateSecurityXml(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        String securityXML = ((SecurityDescriptorModel) artifactoryRequest.getImodel()).getSecurityXML();
        if (StringUtils.isEmpty(securityXML)) {
            artifactoryResponse.error("Cannot save null or empty security configuration.");
        } else {
            try {
                securityService.importSecurityData(securityXML);
                artifactoryResponse.info("Security configuration successfully saved.");
            } catch (Exception e) {
                log.error("Error while manually saving the security configuration.", e);
                artifactoryResponse.error("Unable to save configuration, Please verify the validity of your input");
            }
        }
    }
}
