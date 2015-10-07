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

package org.artifactory.webapp.wicket.page.security.acl;

import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.AceInfo;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableAceInfo;
import org.artifactory.security.MutableAclInfo;
import org.artifactory.security.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yossi Shaul
 */
public class PermissionTargetAceInfoRowDataProvider extends BaseSortableAceInfoRowDataProvider {
    private MutableAclInfo aclInfo;
    private UserGroupService userGroupService;

    public PermissionTargetAceInfoRowDataProvider(UserGroupService userGroupService, MutableAclInfo aclInfo) {
        this.userGroupService = userGroupService;
        this.aclInfo = aclInfo;
        loadData();
    }

    @Override
    public void loadData() {
        //Restore the roles
        Set<MutableAceInfo> aceInfos = aclInfo.getMutableAces();
        Map<AceInfo, MutableAceInfo> acesMap = new HashMap<>(aceInfos.size());
        for (MutableAceInfo aceInfo : aceInfos) {
            acesMap.put(aceInfo, aceInfo);
        }
        //List of recipients except for admins that are filtered out from acl management
        List<UserInfo> users = getUsers();
        List<GroupInfo> groups = getGroups();
        //Create a list of acls for *all* users and groups
        //Stored acls are only the non empty ones
        List<AceInfoRow> rows = new ArrayList<>(users.size());
        for (UserInfo user : users) {
            addAceRow(rows, acesMap, user.getUsername(), false);
        }
        for (GroupInfo group : groups) {
            addAceRow(rows, acesMap, group.getGroupName(), true);
        }
        this.aces = rows;
    }

    protected List<GroupInfo> getGroups() {
        return userGroupService.getAllGroups();
    }

    protected List<UserInfo> getUsers() {
        return userGroupService.getAllUsers(false);
    }

    private void addAceRow(List<AceInfoRow> rows, Map<AceInfo, MutableAceInfo> aces, String username, boolean group) {
        MutableAceInfo aceInfo = InfoFactoryHolder.get().createAce(username, group, 0);
        MutableAceInfo existingAceInfo = aces.get(aceInfo);
        if (existingAceInfo == null) {
            aclInfo.getMutableAces().add(aceInfo);
        } else {
            aceInfo = existingAceInfo;
        }
        AceInfoRow row = AceInfoRow.createMutableAceInfoRow(aceInfo);
        rows.add(row);
    }

    public void setAclInfo(MutableAclInfo aclInfo) {
        this.aclInfo = aclInfo;
    }
}