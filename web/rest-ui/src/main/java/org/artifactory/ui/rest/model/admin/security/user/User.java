package org.artifactory.ui.rest.model.admin.security.user;

import org.artifactory.security.UserGroupInfo;
import org.artifactory.ui.rest.model.admin.security.group.UserGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * @author chen keinan
 */
public class User extends BaseUser{

    private Set<UserGroup> userGroups;
    private String externalRealmStatus;

    public User() {}

    public Set<UserGroupInfo> getUserGroups() {
       if (userGroups == null){
           return null;
       }

        Set<UserGroupInfo> userGroupInfos = new HashSet<>();
        for (UserGroup group : userGroups){
            userGroupInfos.add(group);
        }
        return userGroupInfos;
    }

    public String getExternalRealmStatus() {
        return externalRealmStatus;
    }

    public void setExternalRealmStatus(String externalRealmStatus) {
        this.externalRealmStatus = externalRealmStatus;
    }
}
