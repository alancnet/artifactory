package org.artifactory.ui.rest.service.admin.security.saml;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.common.SecurityModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSamlService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RestModel restModel = getSamlModel();
        response.iModel(restModel);
    }

    /**
     * get saml setting and populate it data to saml model
     *
     * @return - saml model
     */
    private RestModel getSamlModel() {
        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SecurityDescriptor securityDescriptor = centralConfig.getSecurity();
        SamlSettings samlSettings = securityDescriptor.getSamlSettings();
        return SecurityModelPopulator.populateSamlInfo(samlSettings);
    }
}
