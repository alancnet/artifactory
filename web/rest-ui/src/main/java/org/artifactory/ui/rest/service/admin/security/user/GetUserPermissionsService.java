package org.artifactory.ui.rest.service.admin.security.user;

import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AceInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.admin.security.user.UserPermissions;
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
public class GetUserPermissionsService implements RestService<User> {

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    AclService aclService;

    @Override
    public void execute(ArtifactoryRestRequest<User> request, RestResponse response) {
        String name = request.getPathParamByKey("id");
        boolean userOnly = Boolean.valueOf(request.getQueryParamByKey("userOnly"));
        // get user related permissions
        List<UserPermissions> userPermissionsList = getUserRelatedPermissions(name, userOnly);
        // update response model
        response.iModelList(userPermissionsList);
    }


    /**
     * get user related permission by user name
     *
     * @param name     - user name
     * @param userOnly - if true user related permission only , else user and group related permissions
     * @return - list of user related permissions data
     */
    private List<UserPermissions> getUserRelatedPermissions(String name, boolean userOnly) {
        Map<PermissionTargetInfo, AceInfo> userPermissionByPrincipal;
        List<UserPermissions> userPermissionsList = new ArrayList<>();
        if (!userOnly) {
            userPermissionByPrincipal = aclService.getUserPermissionByPrincipal(name);
        } else {
            userPermissionByPrincipal = aclService.getUserPermissions(name);
        }
        userPermissionByPrincipal.forEach((permission, ace) ->
                userPermissionsList.add(new UserPermissions(ace, permission)));
        return userPermissionsList;
    }
}
