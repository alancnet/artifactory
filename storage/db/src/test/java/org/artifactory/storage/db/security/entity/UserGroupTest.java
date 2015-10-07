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

package org.artifactory.storage.db.security.entity;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Date: 8/26/12
 * Time: 11:46 PM
 *
 * @author freds
 */
@Test
public class UserGroupTest {

    public void simpleUserGroupTest() {
        UserGroup ug = new UserGroup(1L, 1L, null);
        assertEquals(ug.getUserId(), 1L);
        assertEquals(ug.getGroupId(), 1L);
        assertNull(ug.getRealm());
    }

    public void withRealmUserGroupTest() {
        UserGroup ug = new UserGroup(2L, 2L, "ldap");
        assertEquals(ug.getUserId(), 2L);
        assertEquals(ug.getGroupId(), 2L);
        assertEquals(ug.getRealm(), "ldap");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noUserUserGroupTest() {
        new UserGroup(-2L, 2L, "ldap");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noGroupUserGroupTest() {
        new UserGroup(1L, 0L, "ldap");
    }

    public void EqualsUserGroupTest() {
        UserGroup ug1 = new UserGroup(1L, 1L, null);
        UserGroup ug2a = new UserGroup(1L, 2L, null);
        UserGroup ug2b = new UserGroup(2L, 1L, null);
        UserGroup ug3 = new UserGroup(1L, 1L, "ldap");

        assertNotEquals(ug1, ug2a);
        assertNotEquals(ug1.hashCode(), ug2a.hashCode());
        assertNotEquals(ug1, ug2b);
        assertNotEquals(ug1.hashCode(), ug2b.hashCode());
        assertNotEquals(ug3, ug2a);
        assertNotEquals(ug3.hashCode(), ug2a.hashCode());
        assertNotEquals(ug3, ug2b);
        assertNotEquals(ug3.hashCode(), ug2b.hashCode());
        assertEquals(ug1, ug3);
        assertEquals(ug1.hashCode(), ug3.hashCode());
    }
}
