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

package org.artifactory.storage.security.service;

import org.artifactory.sapi.common.Lock;
import org.artifactory.security.AclInfo;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableAclInfo;
import org.artifactory.security.UserInfo;

import java.util.Collection;

/**
 * Date: 9/3/12
 * Time: 4:13 PM
 *
 * @author freds
 */
public interface AclStoreService {

    /**
     * @return Returns all the AclInfos
     */
    Collection<AclInfo> getAllAcls();

    @Lock
    void createAcl(AclInfo entity);

    @Lock
    void updateAcl(MutableAclInfo acl);

    @Lock
    void deleteAcl(String permTargetName);

    AclInfo getAcl(String permTargetName);

    boolean permissionTargetExists(String permTargetName);

    boolean userHasPermissions(String username);

    @Lock
    void removeAllUserAces(String username);

    @Lock
    void createDefaultSecurityEntities(UserInfo anonUser, GroupInfo readersGroup, String currentUsername);

    @Lock
    void deleteAllAcls();

    void removeAllGroupAces(String groupName);

    int promoteAclsDbVersion();
}
