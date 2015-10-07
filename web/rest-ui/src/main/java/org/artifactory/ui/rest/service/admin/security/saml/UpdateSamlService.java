package org.artifactory.ui.rest.service.admin.security.saml;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.saml.SamlSsoAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.SamlSettings;
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
public class UpdateSamlService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        //update saml setting
        updateSamlSetting(request, response);
    }

    /**
     * update saml setting
     *
     * @param artifactoryRequest - encapsulate data related to request
     */
    private void updateSamlSetting(ArtifactoryRestRequest artifactoryRequest, RestResponse response) {
        try {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            SamlSsoAddon samlSsoAddon = addonsManager.addonByType(SamlSsoAddon.class);
            SamlSettings samlSettings = (SamlSettings) artifactoryRequest.getImodel();
            if (samlSettings.isEnableIntegration()) {
                samlSsoAddon.createCertificate(samlSettings.getCertificate());
            }
            MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
            SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
            samlSettings.setCertificate(samlSettings.getCertificate());
            securityDescriptor.setSamlSettings(samlSettings);
            centralConfigService.saveEditedDescriptorAndReload(centralConfig);
            response.info("Successfully updated SAML SSO settings");
        } catch (Exception e) {
            response.error(e.getMessage());
        }
    }
}
