/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.model.xstream.security;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;

/**
 * Holds information about user groups.
 *
 * @author Yossi Shaul
 */
@XStreamAlias("group")
public class GroupImpl implements MutableGroupInfo {
    private String groupName;
    private String description;

    /**
     * indicates if this group should automatically be added to newly created users
     */
    private boolean newUserDefault;

    private String realm;

    private String realmAttributes;

    public GroupImpl() {
        this.realm = SecurityConstants.DEFAULT_REALM;
    }

    public GroupImpl(String groupName) {
        this.groupName = groupName;
        this.realm = SecurityConstants.DEFAULT_REALM;
    }

    public GroupImpl(String groupName, String description, boolean newUserDefault) {
        this.groupName = groupName;
        this.description = description;
        this.newUserDefault = newUserDefault;
        this.realm = SecurityConstants.DEFAULT_REALM;
    }

    public GroupImpl(String groupName, String description, String realm) {
        this.groupName = groupName;
        this.description = description;
        this.realm = realm != null ? realm : SecurityConstants.DEFAULT_REALM;
    }

    public GroupImpl(String groupName, String description,
            boolean newUserDefault, String realm,
            String realmAttributes) {
        this.groupName = groupName;
        this.description = description;
        this.newUserDefault = newUserDefault;
        this.realm = realm != null ? realm : SecurityConstants.DEFAULT_REALM;
        this.realmAttributes = realmAttributes;
    }

    /**
     * A copy constructor.
     *
     * @param groupInfo Original group info.
     */
    public GroupImpl(GroupInfo groupInfo) {
        this(groupInfo.getGroupName(),
                groupInfo.getDescription(),
                groupInfo.isNewUserDefault(),
                groupInfo.getRealm(),
                groupInfo.getRealmAttributes());
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return True if this group should automatically be added to newly created users.
     */
    @Override
    public boolean isNewUserDefault() {
        return newUserDefault;
    }

    @Override
    public void setNewUserDefault(boolean newUserDefault) {
        this.newUserDefault = newUserDefault;
    }

    @Override
    public boolean isExternal() {
        return realm != null && !SecurityConstants.DEFAULT_REALM.equals(realm);
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public void setRealm(String realm) {
        this.realm = realm != null ? realm : SecurityConstants.DEFAULT_REALM;
    }

    @Override
    public String getRealmAttributes() {
        return realmAttributes;
    }

    @Override
    public void setRealmAttributes(String realmAttributes) {
        this.realmAttributes = realmAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupImpl info = (GroupImpl) o;

        return !(groupName != null ? !groupName.equals(info.groupName) : info.groupName != null);

    }

    @Override
    public int hashCode() {
        return (groupName != null ? groupName.hashCode() : 0);
    }

    @Override
    public String toString() {
        return (groupName != null ? groupName : "Group name not set");
    }

}
