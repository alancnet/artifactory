package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.common.SecurityModelPopulator;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetCrowdIntegrationService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CrowdSettings crowdSettings = getCrowdSettingsFromDescriptor();
        // populate crowd setting to model
        CrowdIntegration crowdConfiguration = SecurityModelPopulator.getCrowdConfiguration(crowdSettings);
        // update response with model
        response.iModel(crowdConfiguration);
    }

    /**
     * get crowd setting from descriptor
     *
     * @return crowd setting model
     */
    private CrowdSettings getCrowdSettingsFromDescriptor() {
        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
        return securityDescriptor.getCrowdSettings();
    }
}
