package org.artifactory.ui.rest.service.admin.security.ldap.groups;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ldapgroup.LdapUserGroup;
import org.artifactory.addon.ldapgroup.LdapUserGroupAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapGroupModel;
import org.artifactory.ui.rest.model.admin.security.ldap.LdapUserGroupModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RefreshLdapGroupService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RefreshLdapGroupService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String userName = request.getPathParamByKey("name");
        LdapGroupModel ldapGroupSetting = (LdapGroupModel) request.getImodel();
        // refresh ldap group list from ldap server
        refreshLdapGroups(response, userName, ldapGroupSetting);
    }

    /**
     * refresh ldap group list from ldap server
     * @param artifactoryResponse - encapsulate date require for response
     * @param userName - user name to filter by
     * @param ldapGroupSetting - ldap group setting
     */
    private void refreshLdapGroups(RestResponse artifactoryResponse, String userName, LdapGroupModel ldapGroupSetting) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LdapUserGroupAddon ldapGroupWebAddon = addonsManager.addonByType(LdapUserGroupAddon.class);
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        Set<LdapUserGroup> ldapGroups = ldapGroupWebAddon.refreshLdapGroups(userName, ldapGroupSetting, statusHolder);
        if (ldapGroups != null && !ldapGroups.isEmpty()) {
            populateLdapGroupToUserGroupModel(artifactoryResponse, statusHolder, ldapGroups);
        } else {
            if (StringUtils.isNotBlank(userName)) {
                artifactoryResponse.error("Could not find DN for user '" + userName + "'");
            } else {
                artifactoryResponse.error("Could not find LDAP groups");
            }
        }
    }

    /**
     * populate ldap user groups to user groups model
     *
     * @param artifactoryResponse - encapsulate date require for response
     * @param statusHolder        -  ldap user refresh action status holder
     * @param ldapGroups          - ldap groups
     */
    private void populateLdapGroupToUserGroupModel(RestResponse artifactoryResponse, BasicStatusHolder statusHolder,
            Set<LdapUserGroup> ldapGroups) {
        log.debug("Retrieved {} groups from LDAP", ldapGroups.size());
        if (statusHolder.isError()) {
            artifactoryResponse.error(statusHolder.getLastError().getMessage());
        } else if (statusHolder.getWarnings().size() != 0) {
            artifactoryResponse.error(statusHolder.getWarnings().get(0).getMessage());
        } else {
            List<GroupInfo> groupInfos = userGroupService.getAllGroups();
            List<LdapUserGroupModel> ldapGroupModels = new ArrayList<>();
            ldapGroups.forEach(group -> {
                MutableGroupInfo artifactoryGroup = InfoFactoryHolder.get().createGroup(group.getGroupName());
                LdapUserGroupModel groupModel = new LdapUserGroupModel(group.getGroupName(),
                        group.getDescription(), group.getGroupDn());
                int groupIndex = groupInfos.indexOf(artifactoryGroup);
                // update group status
                updateGroupStatus(groupInfos, group, groupModel, groupIndex);
                // add group model to list
                ldapGroupModels.add(groupModel);
            });
            artifactoryResponse.info("Successfully retrieved LDAP groups");
            artifactoryResponse.iModelList(ldapGroupModels);
        }
    }

    /**
     * update group status data
     *
     * @param groups     - artifactory groups
     * @param group      - ldap group
     * @param groupModel - ldap group model
     * @param groupIndex - group index in artifactory group
     */
    private void updateGroupStatus(List<GroupInfo> groups, LdapUserGroup group, LdapUserGroupModel groupModel,
            int groupIndex) {
        if (groupIndex != -1) {
            GroupInfo groupInfo = groups.get(groupIndex);
            String realmAttributes = groupInfo.getRealmAttributes();
            String dn = getDnFromRealmAttributes(realmAttributes);
            if (!dn.equals(group.getGroupDn())) {
                groupModel.setRequiredUpdate(LdapUserGroup.Status.REQUIRES_UPDATE);
            } else {
                groupModel.setRequiredUpdate(LdapUserGroup.Status.IN_ARTIFACTORY);
            }
        } else {
            groupModel.setRequiredUpdate(LdapUserGroup.Status.DOES_NOT_EXIST);
        }
    }

    /**
     * Get the group DN from a group info realm attributes.
     *
     * @param realmAttributes The realm attributes.
     * @return The group DN
     */
    public static String getDnFromRealmAttributes(String realmAttributes) {
        if (isBlank(realmAttributes)) {
            log.warn(
                    "Realm attributes are empty, group was probably created manually in Artifactory, needs to be updated");
            return "";
        }
        int startIndexDn = realmAttributes.indexOf("groupDn=");
        if (startIndexDn == -1) {
            return "";
        }
        String groupDn = realmAttributes.substring(startIndexDn, realmAttributes.length());
        startIndexDn = groupDn.indexOf('=');
        if (startIndexDn == -1) {
            return "";
        }
        return groupDn.substring(startIndexDn + 1, groupDn.length());
    }
}
