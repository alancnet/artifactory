package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
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
public class UpdateCrowdIntegration implements RestService {

    private static final Logger log = LoggerFactory.getLogger(UpdateCrowdIntegration.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CrowdSettings crowdSettings = (CrowdSettings) request.getImodel();
        // save crowd settings to config descriptor
        saveCrowdSettings(response, crowdSettings);
    }

    /**
     * save crowd settings to config descriptor
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param crowdSettings       - crowd settings
     */
    private void saveCrowdSettings(RestResponse artifactoryResponse, CrowdSettings crowdSettings) {
        crowdSettings.setPassword(CryptoHelper.encryptIfNeeded(crowdSettings.getPassword()));
        try {
            MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
            SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
            securityDescriptor.setCrowdSettings(crowdSettings);
            centralConfigService.saveEditedDescriptorAndReload(centralConfig);
            artifactoryResponse.info("Successfully updated Atlassian Crowd settings");
        } catch (Exception e) {
            artifactoryResponse.error("FAiled to save new settings");
            log.error("An error occurred while saving new Crowd SSO settings", e);
        }
    }
}
