package org.artifactory.ui.rest.service.admin.security.ldap.groups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapGroupModel;
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
public class CreateLdapGroupService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CreateLdapGroupService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        LdapGroupModel ldapSetting = (LdapGroupModel) request.getImodel();
        // create ldap group
        createLdapGroup(response, ldapSetting);
    }

    /**
     * create ldap group
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param ldapSetting         - ldap group model
     */
    private void createLdapGroup(RestResponse artifactoryResponse, LdapGroupModel ldapSetting) {
        try {
            MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
            configDescriptor.getSecurity().addLdapGroup(ldapSetting);
            centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
            artifactoryResponse.info("Successfully created LDAP group '" + ldapSetting.getName() + "'");
        } catch (Exception e) {
            log.error("Could not save LDAP group Settings {}", e);
            artifactoryResponse.error(" Could not be save LDAP group'" + ldapSetting.getName() + "'");
        }
    }
}
