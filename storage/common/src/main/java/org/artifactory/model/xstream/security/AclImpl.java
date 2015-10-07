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
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.security.AceInfo;
import org.artifactory.security.AclInfo;
import org.artifactory.security.MutableAceInfo;
import org.artifactory.security.MutableAclInfo;
import org.artifactory.security.PermissionTargetInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Yoav Landman
 */
@XStreamAlias("acl")
public class AclImpl implements MutableAclInfo {

    private PermissionTargetImpl permissionTarget;
    // TODO: verify it's a clean HashSet implementation on all sets
    private Set<AceImpl> aces;
    private String updatedBy;

    public AclImpl() {
        this.permissionTarget = new PermissionTargetImpl();
        this.aces = new HashSet<>();
    }

    public AclImpl(AclInfo copy) {
        this(new PermissionTargetImpl(copy.getPermissionTarget()),
                new HashSet<AceInfo>(), copy.getUpdatedBy());
        for (AceInfo aceInfo : copy.getAces()) {
            aces.add(new AceImpl(aceInfo));
        }
    }

    public AclImpl(PermissionTargetInfo permissionTarget) {
        this.permissionTarget = new PermissionTargetImpl(permissionTarget);
        this.aces = new HashSet<>();
    }

    public AclImpl(PermissionTargetInfo permissionTarget, Set<AceInfo> aces, String updatedBy) {
        this.permissionTarget = new PermissionTargetImpl(permissionTarget);
        this.aces = new HashSet<>();
        for (AceInfo ace : aces) {
            this.aces.add(new AceImpl(ace));
        }
        this.updatedBy = updatedBy;
    }

    @Override
    public PermissionTargetInfo getPermissionTarget() {
        return permissionTarget;
    }

    @Override
    public void setPermissionTarget(PermissionTargetInfo permissionTarget) {
        this.permissionTarget = new PermissionTargetImpl(permissionTarget);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Set<AceInfo> getAces() {
        return ImmutableSet.<AceInfo>copyOf((Set) aces);
    }

    @Override
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public Set<MutableAceInfo> getMutableAces() {
        return (Set<MutableAceInfo>) ((Set) aces);
    }

    @Override
    public void setAces(Set<AceInfo> aces) {
        this.aces.clear();
        if (aces != null) {
            for (AceInfo ace : aces) {
                this.aces.add(new AceImpl(ace));
            }
        }
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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