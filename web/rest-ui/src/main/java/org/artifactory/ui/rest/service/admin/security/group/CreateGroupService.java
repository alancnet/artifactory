package org.artifactory.ui.rest.service.admin.security.group;

import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateGroupService extends BaseGroupService {
    @Autowired
    protected SecurityService securityService;
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableGroupInfo group = (MutableGroupInfo) request.getImodel();
        if (userGroupService.findGroup(group.getGroupName()) != null) {
            response.error("Group '" + group.getGroupName() + "' already exists.");
            return;
        }
        boolean created = userGroupService.createGroup(group);
        addUsertoGroup(((Group) group).getUsersInGroup(), group.getGroupName(), response, userGroupService);
        // update response data
        updateResponse(response, group, created);
    }


    /**
     * create group in DB and update response
     * @param restResponse - encapsulate data require for response
     * @param created - if true group successfully created
     */
    private void updateResponse(RestResponse restResponse,
            MutableGroupInfo group,boolean created) {
        if (!created) {
            String errorMsg = "Error with creating group: " + group.getGroupName() ;
            restResponse.error(errorMsg);
            return;
        }
        else{
            restResponse.info("Successfully created group '" + group.getGroupName() + "'");
            restResponse.responseCode(HttpServletResponse.SC_CREATED);
        }
    }
}
