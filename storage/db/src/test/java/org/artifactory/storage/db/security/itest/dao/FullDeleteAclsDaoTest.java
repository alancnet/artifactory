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

import org.artifactory.storage.db.security.dao.AclsDao;
import org.artifactory.storage.db.security.dao.PermissionTargetsDao;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FullDeleteAclsDaoTest extends SecurityBaseDaoTest {

    @Autowired
    private AclsDao aclsDao;

    @Autowired
    private PermissionTargetsDao permissionTargetsDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/user-group.sql");
        importSql("/sql/acls.sql");
    }

    @Test(expectedExceptions = {SQLException.class})
    public void testDeleteUserWithAce() throws SQLException {
        try {
            // TORE: [by fsi] use the error handling to verify we broke the correct constraint
            userGroupsDao.deleteUser("u1");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Test(expectedExceptions = {SQLException.class},dependsOnMethods = "testDeleteUserWithAce" )
    public void testDeletePermissionTargetWithAcl() throws SQLException {
        try {
            // TORE: [by fsi] use the error handling to verify we broke the correct constraint
            permissionTargetsDao.deletePermissionTarget(1L);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Test(dependsOnMethods = {"testDeletePermissionTargetWithAcl"})
    public void testDeleteAllData() throws SQLException {
        assertEquals(aclsDao.deleteAllAcls(), 8);
        assertEquals(permissionTargetsDao.deleteAllPermissionTargets(), 8);
        assertEquals(userGroupsDao.deleteAllGroupsAndUsers(), 15);

        assertTrue(aclsDao.getAllAcls().isEmpty());
        assertNull(permissionTargetsDao.findPermissionTarget(1L));
        assertTrue(userGroupsDao.getAllUsers(true).isEmpty());
    }

}
