package org.artifactory.ui.rest.service.admin.security.group;

import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AceInfo;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.common.SecurityModelPopulator;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGroupService implements RestService {

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    AclService aclService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        fetchSingleOrMultiGroup(response, request);
    }

    /**
     * fetch single or multi group depend on query and path param
     *
     * @param artifactoryResponse   - encapsulate all data related to response
     * artifactoryRequest - encapsulate data related to request
     */
    private void fetchSingleOrMultiGroup(RestResponse artifactoryResponse,
            ArtifactoryRestRequest artifactoryRequest) {
        String groupName = artifactoryRequest.getPathParamByKey("id");
        boolean isDefaultGroupRequire = Boolean.valueOf(artifactoryRequest.getQueryParamByKey("default"));
        if (isMultiGroup(groupName)) {
            updateResponseWithMultiGroupInfo(artifactoryResponse, isDefaultGroupRequire);
        } else {
            updateResponseWithSingleGroupInfo(artifactoryResponse, groupName);
        }
    }

    /**
     * get Single Group info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     * @param groupName           - group name from path param
     */
    private void updateResponseWithSingleGroupInfo(RestResponse artifactoryResponse, String groupName) {
        Group group = getGroup(groupName);
        List<UserInfo> usersInGroup = userGroupService.findUsersInGroup(groupName);
        if (usersInGroup != null) {
            usersInGroup.forEach(userInfo ->
                    group.getUsersInGroup().add(userInfo.getUsername()));
            artifactoryResponse.iModel(group);
        }
    }

    /**
     * get Multi Group info and update response
     *
     * @param artifactoryResponse   - encapsulate Data require for response
     * @param isDefaultGroupRequire - if true , then need to get default groups only
     */
    private void updateResponseWithMultiGroupInfo(RestResponse artifactoryResponse,
            boolean isDefaultGroupRequire) {
        List<GroupInfo> groupInfos = getGroupInfos(isDefaultGroupRequire);
        // add groups to List
        List<RestModel> groupList = new ArrayList<>();
        groupInfos.stream().forEach(
                groupInfo -> {
                    List<String> groups = new ArrayList<>();
                    groups.add(groupInfo.getGroupName());
                    Group groupConfiguration = SecurityModelPopulator.getGroupConfiguration(groupInfo);
                    Map<PermissionTargetInfo, AceInfo> groupsPermissions = aclService.getGroupsPermissions(groups);
                    List<String> permissions = new ArrayList<>();
                    groupsPermissions.forEach((perm, acl) -> permissions.add(perm.getName()));
                    groupConfiguration.setPermissions(permissions);
                    groupList.add(groupConfiguration);
                });
        artifactoryResponse.iModelList(groupList);
    }

    /**
     * check id require to get single / multi group
     *
     * @param groupName - single group name
     * @return if true require multi group
     */
    private boolean isMultiGroup(String groupName) {
        return groupName == null || groupName.length() == 0;
    }

    /**
     * return group by name
     *
     * @param groupName - group name
     * @return
     */
    private Group getGroup(String groupName) {
        GroupInfo localGroup = userGroupService.findGroup(groupName);
        Group group = SecurityModelPopulator.getGroupConfiguration(localGroup);
        return group;
    }
    /**
     * return all groups / default groups
     *
     * @param isDefaultGroupRequire - is default group require query param
     * @return - list of groups info
     */
    private List<GroupInfo> getGroupInfos(boolean isDefaultGroupRequire) {
        List<GroupInfo> groupInfos;
        if (isDefaultGroupRequire) {
            groupInfos = userGroupService.getNewUserDefaultGroups();
        } else {
            groupInfos = userGroupService.getAllGroups();
        }
        return groupInfos;
    }
}
