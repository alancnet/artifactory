package org.artifactory.ui.rest.service.admin.security.group;

import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateGroupService extends BaseGroupService {
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableGroupInfo group = (MutableGroupInfo) request.getImodel();

        if (isResourceIDNotFound(request)) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // update group model changed data
        updateGroupInfoData(group);
        // remove old group users from db
        removePrevGroupUsers(group);
        // add users to group
        addUsertoGroup(((Group) group).getUsersInGroup(), group.getGroupName(), response, userGroupService);
        //update response
        response.info("Successfully updated group '" + group.getGroupName() + "'");
    }

    /**
     * remove group users before update
     *
     * @param group - group data
     */
    private void removePrevGroupUsers(MutableGroupInfo group) {
        List<UserInfo> usersInGroup = userGroupService.findUsersInGroup(group.getGroupName());
        if (usersInGroup != null && !usersInGroup.isEmpty()) {
            List<String> userInGroupList = new ArrayList<>();
            usersInGroup.forEach(userInGroup -> userInGroupList.add(userInGroup.getUsername()));
            userGroupService.removeUsersFromGroup(group.getGroupName(), userInGroupList);
        }
    }

    /**
     * update group info data
     *
     * @param group - group data to be updated
     */
    private void updateGroupInfoData(MutableGroupInfo group) {
        userGroupService.updateGroup(group);
    }

    /**
     * check if resource id has been send on path param
     * artifactoryRequest - encapsulate data related to request
     *
     * @return if true resource id not found on path param
     */
    private boolean isResourceIDNotFound(ArtifactoryRestRequest artifactoryRequest) {
        String id = artifactoryRequest.getPathParamByKey("id");
        return id == null || id.length() == 0;
    }
}
