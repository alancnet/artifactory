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

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Date: 9/3/12
 * Time: 1:44 PM
 *
 * @author freds
 */
public class Acl {
    private final long aclId;
    private final long permTargetId;
    private final long lastModified;
    private final String lastModifiedBy;

    /**
     * Initialized as null, and can (and should) be set only once
     */
    private ImmutableSet<Ace> aces = null;

    public Acl(long aclId, long permTargetId, long lastModified, String lastModifiedBy) {
        if (aclId <= 0L || permTargetId <= 0L) {
            throw new IllegalArgumentException("Ids cannot be negative");
        }
        this.aclId = aclId;
        this.permTargetId = permTargetId;
        this.lastModified = lastModified;
        this.lastModifiedBy = lastModifiedBy;
    }

    public long getAclId() {
        return aclId;
    }

    public long getPermTargetId() {
        return permTargetId;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public ImmutableSet<Ace> getAces() {
        if (aces == null) {
            throw new IllegalStateException("ACL object was not initialized correctly! ACEs missing.");
        }
        return aces;
    }

    public void setAces(Set<Ace> aces) {
        if (this.aces != null) {
            throw new IllegalStateException("Cannot set ACEs already set!");
        }
        if (aces == null) {
            throw new IllegalArgumentException("Cannot set aces to null");
        }
        this.aces = ImmutableSet.copyOf(aces);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Acl acl = (Acl) o;

        if (aclId != acl.aclId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (aclId ^ (aclId >>> 32));
    }

    @Override
    public String toString() {
        return "Acl{" +
                "aclId=" + aclId +
                ", permTargetId=" + permTargetId +
                ", lastModified=" + lastModified +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", aces=" + aces +
                '}';
    }
}
