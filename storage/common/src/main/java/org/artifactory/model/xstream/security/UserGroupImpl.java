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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.UserGroupInfo;

import java.io.Serializable;

/**
 * An object representing the resolved groups of a user, including the external realm the group may belong to
 */
@XStreamAlias("userGroup")
public class UserGroupImpl implements Serializable, UserGroupInfo {

    final String groupName;

    @XStreamOmitField
    String realm = SecurityConstants.DEFAULT_REALM;

    public UserGroupImpl(String groupName) {
        this(groupName, SecurityConstants.DEFAULT_REALM);
    }

    public UserGroupImpl(String groupName, String realm) {
        this.groupName = groupName;
        this.realm = realm;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public boolean isExternal() {
        return !SecurityConstants.DEFAULT_REALM.equals(realm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserGroupImpl info = (UserGroupImpl) o;
        return groupName.equals(info.groupName);
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }

    @Override
    public String toString() {
        return groupName;
    }
}
