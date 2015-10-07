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

package org.artifactory.webapp.wicket.page.security.user;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.security.UserInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Data provider for the users table.
 *
 * @author Yossi Shaul
 */
class UsersTableDataProvider extends SortableDataProvider<UserModel> {
    private List<UserModel> users;

    private UserGroupService userGroupService;

    private SortParam previousSort;
    private UsersFilterPanel usersFilterPanel;

    UsersTableDataProvider(UsersFilterPanel usersFilterPanel, UserGroupService userGroupService) {
        this.usersFilterPanel = usersFilterPanel;
        this.userGroupService = userGroupService;
        //Set default sort
        setSort("username", SortOrder.ASCENDING);
        previousSort = getSort();
        recalcUsersList();
    }

    public List<UserModel> getUsers() {
        return users;
    }

    @Override
    public Iterator<UserModel> iterator(int first, int count) {
        if (!previousSort.equals(getSort())) {
            sortUsers();
        }
        List<UserModel> usersSubList = users.subList(first, first + count);
        return usersSubList.iterator();
    }

    @Override
    public int size() {
        return users.size();
    }

    @Override
    public IModel<UserModel> model(UserModel userModel) {
        return new Model<>(userModel);
    }

    public void recalcUsersList() {
        users = getFilteredUsers();
        sortUsers();
    }

    private void sortUsers() {
        previousSort = getSort();
        if (users != null) {
            ListPropertySorter.sort(users, getSort());
        }
    }

    private List<UserModel> getFilteredUsers() {
        // get selected users
        Set<UserModel> selectedUsers = getSelectedUsers();

        List<UserInfo> allUsers = userGroupService.getAllUsers(true);
        List<UserModel> filtered = new ArrayList<>();
        for (UserInfo userInfo : allUsers) {
            //Send the address for logging purposes
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
            //Don't list excluded users
            if (!addons.isAolAdmin(userInfo) && includedByFilter(userInfo)) {
                UserModel userModel = new UserModel(userInfo);
                filtered.add(userModel);

                // persist selection
                if (selectedUsers.contains(userModel)) {
                    userModel.setSelected(true);
                }
            }
        }
        return filtered;
    }

    private Set<UserModel> getSelectedUsers() {
        Set<UserModel> selectedUsers = new HashSet<>();
        if (users != null) {
            for (UserModel userModel : users) {
                if (userModel.isSelected()) {
                    selectedUsers.add(userModel);
                }
            }
        }
        return selectedUsers;
    }

    /**
     * @param userInfo The user to check if to include in the table
     * @return True if the user should be included
     */
    private boolean includedByFilter(UserInfo userInfo) {
        return passesUsernameFilter(userInfo) && passesGroupNameFilter(userInfo);
    }

    private boolean passesUsernameFilter(UserInfo userInfo) {
        String usernameFilter = usersFilterPanel.getUsernameFilter();
        return (usernameFilter == null || userInfo.getUsername().contains(usernameFilter));
    }

    private boolean passesGroupNameFilter(UserInfo userInfo) {
        String groupNameFilter = usersFilterPanel.getGroupFilter();
        return (groupNameFilter == null || userInfo.isInGroup(groupNameFilter));
    }


}
