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

package org.artifactory.storage.db.security.entity;

/**
 * The link between a User and a Group
 * Date: 8/26/12
 * Time: 11:04 PM
 *
 * @author freds
 */
public class UserGroup {
    private final long userId;
    private final long groupId;
    private final String realm;

    public UserGroup(long userId, long groupId, String realm) {
        if (userId <= 0L) {
            throw new IllegalArgumentException("User id cannot be zero or negative!");
        }
        if (groupId <= 0L) {
            throw new IllegalArgumentException("Group id cannot be zero or negative!");
        }
        this.userId = userId;
        this.groupId = groupId;
        this.realm = realm;
    }

    public long getUserId() {
        return userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public String getRealm() {
        return realm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserGroup userGroup = (UserGroup) o;

        if (groupId != userGroup.groupId) {
            return false;
        }
        if (userId != userGroup.userId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (int) (groupId ^ (groupId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "UserGroup{" +
                "userId=" + userId +
                ", groupId=" + groupId +
                ", realm='" + realm + '\'' +
                '}';
    }
}
