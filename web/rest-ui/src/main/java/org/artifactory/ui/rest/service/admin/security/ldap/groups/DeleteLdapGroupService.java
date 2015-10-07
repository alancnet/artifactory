package org.artifactory.ui.rest.service.admin.security.ldap.groups;

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
public class DeleteLdapGroupService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String groupId = request.getPathParamByKey("id");
        // delete ldap group
        deleteLdapGroup(groupId, response);
    }

    /**
     * delete ldap group
     *
     * @param groupId - group id to be deleted
     */
    private void deleteLdapGroup(String groupId, RestResponse artifactoryResponse) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        configDescriptor.getSecurity().removeLdapGroup(groupId);
        centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
        artifactoryResponse.info("Ldap Group " + groupId + " successfully deleted.");
    }
}
