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
 * @author freds
 */
public class Ace {
    private final long aceId;
    private final long aclId;
    private final int mask;
    private final long userId;
    private final long groupId;

    public Ace(long aceId, long aclId, int mask, long userId, long groupId) {
        if (aceId < 0L || aclId < 0L || userId < 0L || groupId < 0L) {
            throw new IllegalArgumentException("Ids cannot be negative");
        }
        this.aceId = aceId;
        this.aclId = aclId;
        this.mask = mask;
        this.userId = userId;
        this.groupId = groupId;
        if (isOnUser() && isOnGroup()) {
            throw new IllegalArgumentException(
                    "Access Control Entry (ACE) cannot be related to both a group and a user");
        }
        if (!isOnUser() && !isOnGroup()) {
            throw new IllegalArgumentException(
                    "Access Control Entry (ACE) needs to be related to either a group or a user");
        }
    }

    public long getAceId() {
        return aceId;
    }

    public long getAclId() {
        return aclId;
    }

    public int getMask() {
        return mask;
    }

    public long getUserId() {
        return userId;
    }

    public long getGroupId() {
        return groupId;
    }

    /**
     * @return True if this ace belongs to a user
     */
    public final boolean isOnUser() {
        return userId > 0;
    }

    /**
     * @return True if this ace belongs to a group
     */
    public final boolean isOnGroup() {
        return groupId > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Ace ace = (Ace) o;

        if (aceId != ace.aceId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (aceId ^ (aceId >>> 32));
    }

    @Override
    public String toString() {
        return "Ace{" +
                "aceId=" + aceId +
                ", aclId=" + aclId +
                ", mask=" + mask +
                ", userId=" + userId +
                ", groupId=" + groupId +
                '}';
    }
}
