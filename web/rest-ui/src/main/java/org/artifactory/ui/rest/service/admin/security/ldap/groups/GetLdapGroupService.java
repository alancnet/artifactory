package org.artifactory.ui.rest.service.admin.security.ldap.groups;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapGroupModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetLdapGroupService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String groupId = request.getPathParamByKey("id");
        // fetch ldap groups for view or edit
        fetchLdapGroupsForViewOrEdit(response, groupId);
    }

    /**
     * fetch ldap group for view or edit
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param groupId             - group id
     */
    private void fetchLdapGroupsForViewOrEdit(RestResponse artifactoryResponse, String groupId) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        if (groupId.length() == 0) {
            // fetch ldap groups for view
            fetchLdapGroupsForView(artifactoryResponse, configDescriptor);
        } else {
            // fetch ldap groups for edit
            fetchLdapGroupForEdit(artifactoryResponse, groupId, configDescriptor);
        }
    }

    /**
     * fetch ldap group for edit
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param groupId             - group id
     * @param configDescriptor    - config descriptor
     */
    private void fetchLdapGroupForEdit(RestResponse artifactoryResponse, String groupId,
            MutableCentralConfigDescriptor configDescriptor) {
        List<LdapGroupSetting> collect = configDescriptor.getSecurity().getLdapGroupSettings().stream().filter(
                ldapGroupSetting -> ldapGroupSetting.getName().equals(groupId)).collect(
                Collectors.toList());
        LdapGroupModel ldapGroupModel = new LdapGroupModel(collect.get(0), false);
        artifactoryResponse.iModel(ldapGroupModel);
    }

    /**
     * fetch ldap group for view
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param configDescriptor    - config descriptor
     */
    private void fetchLdapGroupsForView(RestResponse artifactoryResponse,
            MutableCentralConfigDescriptor configDescriptor) {
        List<LdapGroupModel> ldapGroupModels = new ArrayList<>();
        List<LdapGroupSetting> ldapGroupSettings = configDescriptor.getSecurity().getLdapGroupSettings();
        ldapGroupSettings.forEach(
                ldapGroupSetting -> ldapGroupModels.add(new LdapGroupModel(ldapGroupSetting, true)));
        artifactoryResponse.iModelList(ldapGroupModels);
    }
}
