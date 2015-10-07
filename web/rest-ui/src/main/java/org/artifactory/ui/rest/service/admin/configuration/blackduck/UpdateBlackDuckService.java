package org.artifactory.ui.rest.service.admin.configuration.blackduck;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.descriptor.external.ExternalProvidersDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateBlackDuckService implements RestService {
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // update black suck setting
        updateBlackDuckSetting(request);
        // update response feedback
        response.info("Successfully updated Black Duck settings");
    }

    /**
     * update central config with and external provider with black duck setting
     *
     * @param artifactoryRequest
     */
    private void updateBlackDuckSetting(ArtifactoryRestRequest artifactoryRequest) {
        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        BlackDuckSettingsDescriptor blackDuckSettingsDescriptor = (BlackDuckSettingsDescriptor) artifactoryRequest.getImodel();
        if (blackDuckSettingsDescriptor != null) {
            setExternalProviderWithBlackDuckSetting(blackDuckSettingsDescriptor, centralConfig);
            centralConfigService.saveEditedDescriptorAndReload(centralConfig);
        }
    }

    /**
     * set external provider with black duck setting
     *
     * @param blackDuckSettingsDescriptor - black duck setting descriptor
     */
    private void setExternalProviderWithBlackDuckSetting(BlackDuckSettingsDescriptor blackDuckSettingsDescriptor,
            MutableCentralConfigDescriptor centralConfig) {
        ExternalProvidersDescriptor external = centralConfig.getExternalProvidersDescriptor();
        if (external == null) {
            external = new ExternalProvidersDescriptor();
        }
        external.setBlackDuckSettingsDescriptor(blackDuckSettingsDescriptor);
        centralConfig.setExternalProvidersDescriptor(external);
    }
}
