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

package org.artifactory.storage.db.security.service;

import org.artifactory.api.security.GroupNotFoundException;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.common.Info;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.security.dao.UserGroupsDao;
import org.artifactory.storage.db.security.dao.UserPropertiesDao;
import org.artifactory.storage.db.security.entity.Group;
import org.artifactory.storage.db.security.entity.User;
import org.artifactory.storage.db.security.entity.UserGroup;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Date: 8/27/12
 * Time: 2:47 PM
 *
 * @author freds
 */
@Service
public class UserGroupServiceImpl implements UserGroupStoreService {
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = LoggerFactory.getLogger(UserGroupServiceImpl.class);

    @Autowired
    private DbService dbService;

    @Autowired
    private UserGroupsDao userGroupsDao;

    @Autowired
    private UserPropertiesDao userPropertiesDao;

    @Override
    public void deleteAllGroupsAndUsers() {
        try {
            userGroupsDao.deleteAllGroupsAndUsers();
        } catch (SQLException e) {
            throw new StorageException("Could not delete all users and groups", e);
        }
    }

    @Override
    public boolean adminUserExists() {
        try {
            return userGroupsDao.adminUserExists();
        } catch (SQLException e) {
            throw new StorageException("Could not determine if admin users exists due to: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean userExists(String username) {
        try {
            return userGroupsDao.findUserIdByUsername(username) > 0L;
        } catch (SQLException e) {
            throw new StorageException("Could not execute exists query for username='" + username + "'", e);
        }
    }

    @Override
    public UserInfo findUser(String username) {
        try {
            User user = userGroupsDao.findUserByName(username);
            if (user != null) {
                return userToUserInfo(user);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not execute search query for username='" + username + "'", e);
        }
        return null;
    }

    @Override
    public void updateUser(MutableUserInfo userInfo) {
        try {
            User originalUser = userGroupsDao.findUserByName(userInfo.getUsername());
            if (originalUser == null) {
                throw new UsernameNotFoundException(
                        "Cannot update user with user name '" + userInfo.getUsername() + "' since it does not exists!");
            }
            User updatedUser = userInfoToUser(originalUser.getUserId(), userInfo);
            userGroupsDao.updateUser(updatedUser);
        } catch (SQLException e) {
            throw new StorageException("Failed to update user " + userInfo.getUsername(), e);
        }
    }

    @Override
    public boolean createUser(UserInfo user) {
        try {
            if (userExists(user.getUsername())) {
                return false;
            }
            User u = userInfoToUser(dbService.nextId(), user);
            return userGroupsDao.createUser(u) > 0;
        } catch (SQLException e) {
            throw new StorageException("Failed to create user " + user.getUsername(), e);
        }
    }

    @Override
    public void deleteUser(String username) {
        try {
            userGroupsDao.deleteUser(username);
        } catch (SQLException e) {
            throw new StorageException("Failed to delete user " + username, e);
        }
    }

    @Override
    public List<UserInfo> getAllUsers(boolean includeAdmins) {
        List<UserInfo> results = new ArrayList<>();
        try {
            Collection<User> allUsers = userGroupsDao.getAllUsers(includeAdmins);
            for (User user : allUsers) {
                results.add(userToUserInfo(user));
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not execute get all users query", e);
        }
    }

    @Override
    public Collection<Info> getUsersGroupsPaging(boolean includeAdmins, String orderBy,
            String startOffset, String limit, String direction) {
        Collection<Info> infoCollection;
        try {
            infoCollection = userGroupsDao.getUsersGroupsPaging(includeAdmins, orderBy,
                    startOffset, limit, direction);
        } catch (SQLException e) {
            throw new StorageException("Failed to get users  group ");
        }
        return infoCollection;
    }

    public long getAllUsersGroupsCount(boolean includeAdmins) {
        return userGroupsDao.getAllUsersGroupsCount(includeAdmins);
    }

    @Override
    public boolean deleteGroup(String groupName) {
        try {
            return userGroupsDao.deleteGroup(groupName) > 0;
        } catch (SQLException e) {
            throw new StorageException("Failed to delete group " + groupName, e);
        }
    }

    private List<GroupInfo> findAllGroups(UserGroupsDao.GroupFilter groupFilter) {
        List<GroupInfo> results = new ArrayList<>();
        try {
            Collection<Group> allGroups = userGroupsDao.findGroups(groupFilter);
            for (Group group : allGroups) {
                results.add(groupToGroupInfo(group));
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not execute get all groups query", e);
        }
    }

    @Override
    public List<GroupInfo> getAllGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.ALL);
    }

    @Override
    public List<GroupInfo> getNewUserDefaultGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.DEFAULTS);
    }

    @Override
    public List<GroupInfo> getAllExternalGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.EXTERNAL);
    }

    @Override
    public List<GroupInfo> getInternalGroups() {
        return findAllGroups(UserGroupsDao.GroupFilter.INTERNAL);
    }

    @Override
    public Set<String> getNewUserDefaultGroupsNames() {
        Set<String> results = new HashSet<>();
        try {
            Collection<Group> allGroups = userGroupsDao.findGroups(UserGroupsDao.GroupFilter.DEFAULTS);
            for (Group group : allGroups) {
                results.add(group.getGroupName());
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not execute get all default group names query", e);
        }
    }

    @Override
    public void updateGroup(MutableGroupInfo groupInfo) {
        try {
            Group originalGroup = userGroupsDao.findGroupByName(groupInfo.getGroupName());
            if (originalGroup == null) {
                throw new GroupNotFoundException("Cannot update non existent group '" + groupInfo.getGroupName() + "'");
            }
            Group newGroup = groupInfoToGroup(originalGroup.getGroupId(), groupInfo);
            if (userGroupsDao.updateGroup(newGroup) != 1) {
                throw new StorageException("Updating group did not find corresponding entity" +
                        " based on name='" + groupInfo.getGroupName() + "' and id=" + originalGroup.getGroupId());
            }
        } catch (SQLException e) {
            throw new StorageException("Could not update group " + groupInfo.getGroupName(), e);
        }
    }

    @Override
    public boolean createGroup(GroupInfo groupInfo) {
        try {
            if (userGroupsDao.findGroupByName(groupInfo.getGroupName()) != null) {
                // Group already exists
                return false;
            }
            Group g = groupInfoToGroup(dbService.nextId(), groupInfo);
            return userGroupsDao.createGroup(g) > 0;
        } catch (SQLException e) {
            throw new StorageException("Could not create group " + groupInfo.getGroupName(), e);
        }
    }

    @Override
    public void addUsersToGroup(String groupName, List<String> usernames) {
        try {
            Group group = userGroupsDao.findGroupByName(groupName);
            if (group == null) {
                throw new GroupNotFoundException("Cannot add users to non existent group " + groupName);
            }
            userGroupsDao.addUsersToGroup(group.getGroupId(), usernames, group.getRealm());
        } catch (SQLException e) {
            throw new StorageException("Could not add users " + usernames + " to group " + groupName, e);
        }
    }

    @Override
    public void removeUsersFromGroup(String groupName, List<String> usernames) {
        try {
            Group group = userGroupsDao.findGroupByName(groupName);
            if (group == null) {
                throw new GroupNotFoundException("Cannot remove users to non existent group " + groupName);
            }
            userGroupsDao.removeUsersFromGroup(group.getGroupId(), usernames);
        } catch (SQLException e) {
            throw new StorageException("Could not add users " + usernames + " to group " + groupName, e);
        }
    }

    @Override
    public List<UserInfo> findUsersInGroup(String groupName) {
        List<UserInfo> results = new ArrayList<>();
        try {
            Group group = userGroupsDao.findGroupByName(groupName);
            if (group == null) {
                return results;
            }
            List<User> users = userGroupsDao.findUsersInGroup(group.getGroupId());
            for (User user : users) {
                results.add(userToUserInfo(user));
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not find users for group with name='" + groupName + "'", e);
        }
    }

    @Override
    @Nullable
    public GroupInfo findGroup(String groupName) {
        try {
            Group group = userGroupsDao.findGroupByName(groupName);
            if (group != null) {
                return groupToGroupInfo(group);
            }
            return null;
        } catch (SQLException e) {
            throw new StorageException("Could not search for group with name='" + groupName + "'", e);
        }
    }

    @Override
    @Nullable
    public UserInfo findUserByProperty(String key, String val) {
        try {
            long userId = userPropertiesDao.getUserIdByProperty(key, val);
            if (userId == 0L) {
                return null;
            } else {
                return userToUserInfo(userGroupsDao.findUserById(userId));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not search for user with property " + key + ":" + val, e);
        }
    }

    @Override
    @Nullable
    public String findUserProperty(String username, String key) {
        try {
            return userPropertiesDao.getUserProperty(username, key);
        } catch (SQLException e) {
            throw new StorageException("Could not search for datum " + key + " of user " + username, e);
        }
    }

    @Override
    public boolean addUserProperty(String username, String key, String val) {
        try {
            return userPropertiesDao.addUserProperty(userGroupsDao.findUserIdByUsername(username), key, val);
        } catch (SQLException e) {
            throw new StorageException("Could not add external data " + key + ":" + val + " to user " + username, e);
        }
    }

    @Override
    public boolean deleteUserProperty(String username, String key) {
        try {
            return userPropertiesDao.deleteProperty(userGroupsDao.findUserIdByUsername(username), key);
        } catch (SQLException e) {
            throw new StorageException("Could not delete external data " + key + " from user " + username, e);
        }
    }

    private GroupInfo groupToGroupInfo(Group group) {
        MutableGroupInfo result = InfoFactoryHolder.get().createGroup(group.getGroupName());
        result.setDescription(group.getDescription());
        result.setNewUserDefault(group.isNewUserDefault());
        result.setRealm(group.getRealm());
        result.setRealmAttributes(group.getRealmAttributes());
        return result;
    }

    private Group groupInfoToGroup(long groupId, GroupInfo groupInfo) {
        return new Group(groupId, groupInfo.getGroupName(), groupInfo.getDescription(),
                groupInfo.isNewUserDefault(), groupInfo.getRealm(), groupInfo.getRealmAttributes());
    }

    private UserInfo userToUserInfo(User user) throws SQLException {
        UserInfoBuilder builder = new UserInfoBuilder(user.getUsername());
        Set<UserGroupInfo> groups = new HashSet<>(user.getGroups().size());
        for (UserGroup userGroup : user.getGroups()) {
            Group groupById = userGroupsDao.findGroupById(userGroup.getGroupId());
            if (groupById != null) {
                String groupname = groupById.getGroupName();
                groups.add(InfoFactoryHolder.get().createUserGroup(groupname, userGroup.getRealm()));
            } else {
                log.error("Group ID " + userGroup.getGroupId() + " does not exists!" +
                        " Skipping add group for user " + user.getUsername());
            }
        }
        builder.password(new SaltedPassword(user.getPassword(), user.getSalt())).email(user.getEmail())
                .admin(user.isAdmin()).enabled(user.isEnabled()).updatableProfile(user.isUpdatableProfile())
                .groups(groups);
        MutableUserInfo userInfo = builder.build();
        userInfo.setTransientUser(false);
        userInfo.setGenPasswordKey(user.getGenPasswordKey());
        userInfo.setRealm(user.getRealm());
        userInfo.setPrivateKey(user.getPrivateKey());
        userInfo.setPublicKey(user.getPublicKey());
        userInfo.setLastLoginTimeMillis(user.getLastLoginTimeMillis());
        userInfo.setLastLoginClientIp(user.getLastLoginClientIp());
        userInfo.setLastAccessTimeMillis(user.getLastAccessTimeMillis());
        userInfo.setLastAccessClientIp(user.getLastAccessClientIp());
        userInfo.setBintrayAuth(user.getBintrayAuth());
        return userInfo;
    }

    private User userInfoToUser(long userId, UserInfo userInfo) throws SQLException {
        User u = new User(userId, userInfo.getUsername(), userInfo.getPassword(), userInfo.getSalt(),
                userInfo.getEmail(), userInfo.getGenPasswordKey(),
                userInfo.isAdmin(), userInfo.isEnabled(), userInfo.isUpdatableProfile(), userInfo.getRealm(),
                userInfo.getPrivateKey(),
                userInfo.getPublicKey(), userInfo.getLastLoginTimeMillis(), userInfo.getLastLoginClientIp(),
                userInfo.getLastAccessTimeMillis(), userInfo.getLastAccessClientIp(),
                userInfo.getBintrayAuth());
        Set<UserGroupInfo> groups = userInfo.getGroups();
        Set<UserGroup> userGroups = new HashSet<>(groups.size());
        for (UserGroupInfo groupInfo : groups) {
            Group groupByName = userGroupsDao.findGroupByName(groupInfo.getGroupName());
            if (groupByName != null) {
                userGroups.add(new UserGroup(u.getUserId(), groupByName.getGroupId(), groupInfo.getRealm()));
            } else {
                log.error("Group named " + groupInfo.getGroupName() + " does not exists!" +
                        " Skipping add group for user " + userInfo.getUsername());
            }
        }
        u.setGroups(userGroups);
        return u;
    }

}
