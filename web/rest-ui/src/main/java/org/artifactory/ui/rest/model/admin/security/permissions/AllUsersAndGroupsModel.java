package org.artifactory.ui.rest.model.admin.security.permissions;

import org.artifactory.rest.common.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Shay Yaakov
 */
public class AllUsersAndGroupsModel extends BaseModel {

    private List<String> allGroups = new ArrayList<>();
    private List<String> allUsers = new ArrayList<>();

    public List<String> getAllGroups() {
        return allGroups;
    }

    public void setAllGroups(List<String> allGroups) {
        this.allGroups = allGroups;
    }

    public List<String> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<String> allUsers) {
        this.allUsers = allUsers;
    }
}
