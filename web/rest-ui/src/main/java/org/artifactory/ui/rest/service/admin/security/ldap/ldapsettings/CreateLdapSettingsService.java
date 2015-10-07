package org.artifactory.ui.rest.service.admin.security.ldap.ldapsettings;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapSettingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateLdapSettingsService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        LdapSettingModel ldapSetting = (LdapSettingModel) request.getImodel();
        // create New Ldap Setting
        createNewLdapSetting(response, ldapSetting);
    }

    /**
     * create new ldap settings
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param ldapSetting         - ldap setting model
     */
    private void createNewLdapSetting(RestResponse artifactoryResponse, LdapSettingModel ldapSetting) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.getSecurity().addLdap(ldapSetting);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
        artifactoryResponse.info("Successfully created LDAP settings '" + ldapSetting.getKey() + "'");
    }
}
