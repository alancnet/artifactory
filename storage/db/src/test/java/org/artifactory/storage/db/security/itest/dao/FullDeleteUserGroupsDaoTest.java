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

package org.artifactory.storage.db.security.itest.dao;

import com.google.common.collect.ImmutableSet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
* Date: 11/13/12
* Time: 4:28 PM
*
* @author freds
*/
public class FullDeleteUserGroupsDaoTest extends SecurityBaseDaoTest {

    @BeforeClass
    public void setup() {
        importSql("/sql/user-group.sql");
    }

    public void testDeleteLastAdmin() throws SQLException {
        assertTrue(userGroupsDao.adminUserExists());
        checkUserCollection(userGroupsDao.getAllUsers(true), ImmutableSet.of(1, 2, 3, 15, 16));
        assertEquals(userGroupsDao.deleteUser("u2"), 3);
        assertEquals(userGroupsDao.deleteUser("u3"), 1);
        assertEquals(userGroupsDao.deleteUser("admin"), 1);
        checkUserCollection(userGroupsDao.getAllUsers(true), ImmutableSet.of(1, 15));
        assertFalse(userGroupsDao.adminUserExists());
    }

    @Test(dependsOnMethods = "testDeleteLastAdmin")
    public void testDeleteAllData() throws SQLException {
        assertEquals(userGroupsDao.deleteAllGroupsAndUsers(), 14);
        assertTrue(userGroupsDao.getAllUsers(true).isEmpty());
    }

}
