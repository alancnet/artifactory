package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.permissions.AllUsersAndGroupsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllUsersAndGroupsService implements RestService {

    @Autowired
    UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> allUsers = userGroupService.getAllUsers(false)
                .stream()
                .map(UserInfo::getUsername)
                .collect(Collectors.toList());

        List<String> allGroups = userGroupService.getAllGroups()
                .stream()
                .map(GroupInfo::getGroupName)
                .collect(Collectors.toList());

        AllUsersAndGroupsModel allUsersAndGroups = new AllUsersAndGroupsModel();
        allUsersAndGroups.setAllUsers(allUsers);
        allUsersAndGroups.setAllGroups(allGroups);
        response.iModel(allUsersAndGroups);
    }
}
