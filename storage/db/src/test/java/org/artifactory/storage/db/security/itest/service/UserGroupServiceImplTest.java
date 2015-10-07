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

package org.artifactory.storage.db.security.itest.service;

import com.google.common.collect.Sets;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.UserGroupImpl;
import org.artifactory.security.MutableGroupInfo;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SaltedPassword;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.*;

/**
 * Date: 8/27/12
 * Time: 3:28 PM
 *
 * @author freds
 */
@Test
public class UserGroupServiceImplTest extends DbBaseTest {

    @Autowired
    private UserGroupStoreService userGroupService;

    @BeforeClass
    public void setup() {
        importSql("/sql/user-group.sql");
    }

    public void hasAnonymousUser() {
        UserInfo anonymousUser = userGroupService.findUser(UserInfo.ANONYMOUS);
        assertNotNull(anonymousUser);
        assertEquals(anonymousUser.getUsername(), UserInfo.ANONYMOUS);
        assertFalse(anonymousUser.isAdmin());
        assertTrue(anonymousUser.isEnabled());
    }

    public void hasDefaultAdmin() {
        UserInfo defaultAdmin = userGroupService.findUser(SecurityService.DEFAULT_ADMIN_USER);
        assertNotNull(defaultAdmin);
        assertEquals(defaultAdmin.getUsername(), SecurityService.DEFAULT_ADMIN_USER);
        assertTrue(defaultAdmin.isAdmin());
        assertTrue(defaultAdmin.isEnabled());
    }

    public void createUserTest() {
        //Create user with group
        String userName = "createUserByMutableUserInfo";
        UserInfoBuilder builder = new UserInfoBuilder(userName);
        UserGroupInfo expectedGroup = new UserGroupImpl("g1", "g1realm");
        Set<UserGroupInfo> groups = Sets.newHashSet(expectedGroup);
        builder.password(new SaltedPassword("password", "salt")).email("jfrog@jfrog.com").enabled(
                true).updatableProfile(true).groups(groups);
        MutableUserInfo expectedUser = builder.build();
        boolean success = userGroupService.createUser(expectedUser);
        Assert.assertTrue(success, "Fail to create user");
        UserInfo userFromDB = userGroupService.findUser(userName);

        // Make sure that the user is equals to the user in the db.
        Assert.assertEquals(expectedUser.getUsername(), userFromDB.getUsername());
        Assert.assertEquals(expectedUser.getPassword(), userFromDB.getPassword());
        Assert.assertEquals(expectedUser.getEmail(), userFromDB.getEmail());
        Assert.assertEquals(expectedUser.isEnabled(), userFromDB.isEnabled());
        Assert.assertEquals(expectedUser.isUpdatableProfile(), userFromDB.isUpdatableProfile());

        // Assert group
        Assert.assertEquals(userFromDB.getGroups().size(), 1);
        UserGroupInfo groupFromDb = userFromDB.getGroups().iterator().next();
        Assert.assertEquals(expectedGroup.getRealm(), groupFromDb.getRealm());
        Assert.assertEquals(expectedGroup.getRealm(), groupFromDb.getRealm());
    }

    public void createUserExistingUsername() {
        MutableUserInfo user = new UserInfoBuilder(SecurityService.DEFAULT_ADMIN_USER).build();
        Assert.assertFalse(userGroupService.createUser(user), "Should not be able to create duplicate username");
    }

    public void createGroupExistingGroupName() {
        MutableGroupInfo group = InfoFactoryHolder.get().createGroup("g1");
        Assert.assertFalse(userGroupService.createGroup(group), "Should not be able to create duplicate group name");
    }
}
