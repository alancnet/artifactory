package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.common.SecurityModelPopulator;
import org.artifactory.ui.rest.model.admin.security.user.BaseUser;
import org.artifactory.ui.rest.model.admin.security.user.UserPermissions;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keian
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetUsersService implements RestService {

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    AclService aclService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        fetchSingleOrMultiUser(response, request);
    }

    /**
     * fetch single or multi user info objects
     *
     * @param artifactoryResponse - encapsulate all data require for response
     * @param artifactoryRequest - encapsulate data related to request
     */
    private void fetchSingleOrMultiUser(RestResponse artifactoryResponse,
            ArtifactoryRestRequest artifactoryRequest) {
        String userName = artifactoryRequest.getPathParamByKey("id");
        if (isMultiUser(userName)) {
            List<UserInfo> userInfos = userGroupService.getAllUsers(true);

            //Don't list excluded users
            List<UserInfo> filteredUsers = new ArrayList<>();
            for (UserInfo userInfo : userInfos) {
                AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
                CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
                if (addons.isAolAdmin(userInfo)) {
                    filteredUsers.add(userInfo);
                }
            }
            userInfos.removeAll(filteredUsers);

            // update response with users info data
            updateResponseWithMultiUserInfo(artifactoryResponse, userInfos);
        } else {
            updateResponseWithSingleUserInfo(artifactoryResponse, userName);
        }
    }

    /**
     * get Single User info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     * @param userName            - user name from path param
     */
    private void updateResponseWithSingleUserInfo(RestResponse artifactoryResponse, String userName) {
        BaseUser baseUser = getBaseUser(userName);
        artifactoryResponse.iModel(baseUser);
    }

    /**
     * get Multi User info and update response
     *
     * @param artifactoryResponse - encapsulate Data require for response
     * @param userInfos           - list of all Users Found in DB
     */
    private void updateResponseWithMultiUserInfo(RestResponse artifactoryResponse,
            List<UserInfo> userInfos) {
        List<RestModel> baseUserList = new ArrayList<>();
        DateTimeFormatter dateFormatter = ContextHelper.get().getCentralConfig().getDateFormatter();
        // populate users info data to users model
        userInfos.stream().forEach(
                userInfo -> {
                    BaseUser userConfiguration = SecurityModelPopulator.getUserConfiguration(userInfo,dateFormatter);
                    Map<PermissionTargetInfo, AceInfo> userPermissionMap = aclService.getUserPermissionByPrincipal(
                            userInfo.getUsername());
                    List<UserPermissions> userPermissionsList = new ArrayList();
                    userPermissionMap.forEach((permission, ace) ->
                            userPermissionsList.add(new UserPermissions(ace, permission)));
                    // update permissions
                    userConfiguration.setPermissionsList(userPermissionsList);
                    baseUserList.add(userConfiguration);
                });
        // update response with users model data
        artifactoryResponse.iModelList(baseUserList);
    }

    /**
     * check if  single or multi user is require based on path param data
     *
     * @param userName - path param
     * @return if true require multi user
     */
    private boolean isMultiUser(String userName) {
        return userName == null || userName.length() == 0;
    }

    /**
     * get user from DB by name
     *
     * @param userName - user name
     * @return - user info instance for specific user name
     */
    private BaseUser getBaseUser(String userName) {
        BaseUser baseUser;
        UserInfo singleUser = userGroupService.findUser(userName);
        DateTimeFormatter dateFormatter = ContextHelper.get().getCentralConfig().getDateFormatter();
        baseUser = SecurityModelPopulator.getUserConfiguration(singleUser,dateFormatter);
        return baseUser;
    }
}
