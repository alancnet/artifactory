package org.artifactory.ui.rest.service.admin.security.ldap.groups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateLdapGroupService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateLdapGroupService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        LdapGroupSetting ldapSetting = (LdapGroupSetting) request.getImodel();
        // update ldap group settings
        updateLdapSettings(response, ldapSetting);
    }

    /**
     * update ldap settings
     *
     * @param artifactoryResponse - encapsulate data require forr response
     * @param ldapSetting         ldap group settings
     */
    private void updateLdapSettings(RestResponse artifactoryResponse, LdapGroupSetting ldapSetting) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        try {
            configDescriptor.getSecurity().ldapGroupSettingChanged(ldapSetting);
            centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
            artifactoryResponse.info("Successfully updated LDAP '" + ldapSetting.getName() + "'");
        } catch (Exception e) {
            log.error("Failed to save LDAP group settings {}", e);
            artifactoryResponse.error("Failed to save LDAP '" + ldapSetting.getName() + "'");
        }
    }
}
