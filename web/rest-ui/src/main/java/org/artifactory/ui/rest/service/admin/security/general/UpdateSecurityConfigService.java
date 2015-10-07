package org.artifactory.ui.rest.service.admin.security.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateSecurityConfigService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SecurityConfig editedDescriptor = (SecurityConfig) request.getImodel();
        SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
        updateSecurityDescriptor(editedDescriptor, securityDescriptor);
        centralConfig.setSecurity(securityDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(centralConfig);
        response.info("Successfully updated security settings");
    }

    /**
     * update security descriptor general data
     *
     * @param editedDescriptor   - changed general data
     * @param securityDescriptor - config descriptor from memory
     */
    private void updateSecurityDescriptor(SecurityConfig editedDescriptor, SecurityDescriptor securityDescriptor) {
        securityDescriptor.setAnonAccessEnabled(editedDescriptor.isAnonAccessEnabled());
        securityDescriptor.setAnonAccessToBuildInfosDisabled(editedDescriptor.isAnonAccessToBuildInfosDisabled());
        securityDescriptor.setHideUnauthorizedResources(editedDescriptor.isHideUnauthorizedResources());
        securityDescriptor.setPasswordSettings(editedDescriptor.getPasswordSettings());
    }
}
