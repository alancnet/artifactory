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

package org.artifactory.factory.common;

import org.artifactory.factory.InfoFactory;
import org.artifactory.md.MutableMetadataInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.security.ImmutableAclInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.AceInfo;
import org.artifactory.security.AclInfo;
import org.artifactory.security.MutableAceInfo;
import org.artifactory.security.MutableAclInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.security.MutablePermissionTargetInfo;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.security.UserGroupInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Date: 8/3/11
 * Time: 9:51 AM
 *
 * @author Fred Simon
 */
public abstract class AbstractInfoFactory implements InfoFactory {

    @Override
    public MutablePermissionTargetInfo createPermissionTarget(String permName, List<String> repoKeys) {
        MutablePermissionTargetInfo permissionTarget = createPermissionTarget();
        permissionTarget.setName(permName);
        permissionTarget.setRepoKeys(repoKeys);
        return permissionTarget;
    }

    @Override
    public MutableAceInfo createAce(String principal, boolean group, int mask) {
        MutableAceInfo aceInfo = createAce();
        aceInfo.setPrincipal(principal);
        aceInfo.setGroup(group);
        aceInfo.setMask(mask);
        return aceInfo;
    }

    @Override
    public Set<UserGroupInfo> createGroups(Set<String> names) {
        if (names == null) {
            return null;
        }
        //Create a list of default groups
        Set<UserGroupInfo> userGroupInfos = new HashSet<>(names.size());
        for (String name : names) {
            UserGroupInfo userGroupInfo = createUserGroup(name);
            userGroupInfos.add(userGroupInfo);
        }
        return userGroupInfos;
    }

    @Override
    public MutableGroupInfo createGroup(String groupName) {
        MutableGroupInfo group = createGroup();
        group.setGroupName(groupName);
        group.setRealm(SecurityConstants.DEFAULT_REALM);
        return group;
    }

    @Override
    public MutableUserInfo createUser(String userName) {
        MutableUserInfo user = createUser();
        user.setUsername(userName);
        return user;
    }

    @Override
    public MutableAclInfo createAcl(PermissionTargetInfo permissionTarget) {
        MutableAclInfo acl = createAcl();
        acl.setPermissionTarget(permissionTarget);
        return acl;
    }

    @Override
    public AclInfo createAcl(PermissionTargetInfo permissionTarget, Set<AceInfo> aces, String updatedBy) {
        return new ImmutableAclInfo(permissionTarget, aces, updatedBy);
    }

    @Override
    public MutableMetadataInfo createMetadata(RepoPath repoPath, String metadataName) {
        return createMetadata(new RepoPathImpl(repoPath.getRepoKey(),
                NamingUtils.getMetadataPath(repoPath.getPath(), metadataName)));
    }
}
