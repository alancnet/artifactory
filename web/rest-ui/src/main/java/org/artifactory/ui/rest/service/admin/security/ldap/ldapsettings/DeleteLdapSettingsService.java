package org.artifactory.ui.rest.service.admin.security.ldap.ldapsettings;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteLdapSettingsService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String ldapKey = request.getPathParamByKey("id");
        deleteLdapSetting(ldapKey, response);
    }

    private void deleteLdapSetting(String ldapKey, RestResponse artifactoryResponse) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        mutableDescriptor.getSecurity().removeLdap(ldapKey);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
        artifactoryResponse.info("LDAP " + ldapKey + " successfully deleted");
    }
}
