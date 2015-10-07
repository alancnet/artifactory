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
public class GroupTest {

    public void simpleGroupTest() {
        Group g = new Group(1L, "g1", null, false, null, null);
        assertEquals(g.getGroupId(), 1L);
        assertEquals(g.getGroupName(), "g1");
        assertNull(g.getDescription());
        assertFalse(g.isNewUserDefault());
        assertNull(g.getRealm());
        assertNull(g.getRealmAttributes());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noGroupIdTest() {
        new Group(0L, "XXX", "group description", true, "ldap", "dn=o");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noGroupNameTest() {
        new Group(1L, "", "group description", true, "ldap", "dn=o");
    }

    public void withRealmGroupTest() {
        Group g = new Group(1L, "g1", "group description", true, "ldap", "dn=o");
        assertEquals(g.getGroupId(), 1L);
        assertEquals(g.getGroupName(), "g1");
        assertEquals(g.getDescription(), "group description");
        assertTrue(g.isNewUserDefault());
        assertEquals(g.getRealm(), "ldap");
        assertEquals(g.getRealmAttributes(), "dn=o");
    }
}
