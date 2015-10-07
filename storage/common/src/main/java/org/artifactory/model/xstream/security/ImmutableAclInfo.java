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

import com.google.common.collect.ImmutableSet;
import org.artifactory.security.AceInfo;
import org.artifactory.security.AclInfo;
import org.artifactory.security.PermissionTargetInfo;

import java.util.Set;

/**
 * @author Fred Simon
 */
public class ImmutableAclInfo implements AclInfo {

    private final PermissionTargetInfo permissionTarget;
    private final ImmutableSet<AceInfo> aces;
    private final String updatedBy;

    public ImmutableAclInfo(PermissionTargetInfo permissionTarget, Set<AceInfo> aces, String updatedBy) {
        this.permissionTarget = permissionTarget;
        this.aces = ImmutableSet.copyOf(aces);
        this.updatedBy = updatedBy;
    }

    @Override
    public PermissionTargetInfo getPermissionTarget() {
        return permissionTarget;
    }

    @Override
    public Set<AceInfo> getAces() {
        return aces;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AclInfo)) {
            return false;
        }

        AclInfo info = (AclInfo) o;

        return !(permissionTarget != null ? !permissionTarget.equals(info.getPermissionTarget()) :
                info.getPermissionTarget() != null);
    }

    @Override
    public int hashCode() {
        return (permissionTarget != null ? permissionTarget.hashCode() : 0);
    }
}