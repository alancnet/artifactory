package org.artifactory.ui.rest.service.admin.security.group;

import java.util.List;

import org.artifactory.api.security.GroupNotFoundException;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;

/**
 * @author chen keinan
 */
public abstract class BaseGroupService implements RestService {

    /**
     * @param users     - user list to be added to group
     * @param groupName - group name
     */
    protected void addUsertoGroup(List<String> users, String groupName, RestResponse response,
                                  UserGroupService userGroupService) {
        if (users != null && !users.isEmpty()) {
            try {
                userGroupService.addUsersToGroup(
                        groupName, users);
                response.info("Successfully added selected users to group '" + groupName + "'");
            } catch (GroupNotFoundException gnfe) {
                response.error("Could not find group '" + groupName + "': " + gnfe.getMessage());
            }
        }
    }
}
