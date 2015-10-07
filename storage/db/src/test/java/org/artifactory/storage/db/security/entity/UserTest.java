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

import java.util.HashSet;

import static org.testng.Assert.*;

/**
 * Date: 8/26/12
 * Time: 11:46 PM
 *
 * @author freds
 */
@Test
public class UserTest {

    public void simpleUserTest() {
        User u = new User(1L, "u1", null, "SALT", null, false, true, false, "sdsdf:SDFSDF3434");
        u.setGroups(new HashSet<UserGroup>());
        assertEquals(u.getUserId(), 1L);
        assertEquals(u.getUsername(), "u1");
        assertEquals(u.getSalt(), "SALT");
        assertEquals(u.getBintrayAuth(), "sdsdf:SDFSDF3434");
        assertNull(u.getPassword());
        assertNull(u.getEmail());
        assertFalse(u.isAdmin());
        assertTrue(u.isEnabled());
        assertFalse(u.isUpdatableProfile());
        assertTrue(u.getGroups().isEmpty());
    }

    public void withPassUserTest() {
        User u = new User(2L, "u2", "APASSWD", "SALT", "e@mail.com", true, true, true, "asd:23SDFSD34");
        u.setGroups(new HashSet<UserGroup>());
        assertEquals(u.getUserId(), 2L);
        assertEquals(u.getUsername(), "u2");
        assertEquals(u.getPassword(), "APASSWD");
        assertEquals(u.getSalt(), "SALT");
        assertEquals(u.getBintrayAuth(), "asd:23SDFSD34");
        assertEquals(u.getEmail(), "e@mail.com");
        assertTrue(u.isAdmin());
        assertTrue(u.isEnabled());
        assertTrue(u.isUpdatableProfile());
        assertTrue(u.getGroups().isEmpty());
    }

    public void withGroupsUserTest() {
        User u = new User(3L, "u3", "BPASSWD", "SALT", "e@mail.com", true, true, true, null);
        HashSet<UserGroup> groups = new HashSet<UserGroup>();
        groups.add(new UserGroup(3L, 1L, null));
        u.setGroups(groups);
        assertEquals(u.getUserId(), 3L);
        assertEquals(u.getUsername(), "u3");
        assertEquals(u.getPassword(), "BPASSWD");
        assertEquals(u.getSalt(), "SALT");
        assertEquals(u.getEmail(), "e@mail.com");
        assertTrue(u.isAdmin());
        assertTrue(u.isEnabled());
        assertTrue(u.isUpdatableProfile());
        assertEquals(u.getGroups().size(), 1);
        assertEquals(u.getGroups().iterator().next(), new UserGroup(3L, 1L, "does not matter"));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void negUserIdTest() {
        new User(-1L, "XXX", "APASSWD", "SALT", "e@mail.com", true, true, true, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noUserIdTest() {
        new User(0L, "XXX", "APASSWD", "SALT", "e@mail.com", true, true, true, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noUserNameTest() {
        new User(1L, "", "APASSWD", "SALT", "e@mail.com", true, true, true, null);
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void noGroupsSetTest() {
        User u = new User(1L, "u1", "APASSWD", "SALT", "e@mail.com", true, true, true, null);
        u.getGroups();
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void tooManyGroupsSetTest() {
        User u = new User(1L, "u1", "APASSWD", "SALT", "e@mail.com", true, true, true, null);
        u.setGroups(new HashSet<UserGroup>());
        u.setGroups(new HashSet<UserGroup>());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void wrongGroupSetTest() {
        User u = new User(1L, "u1", "APASSWD", "SALT", "e@mail.com", true, true, true, null);
        HashSet<UserGroup> groups = new HashSet<UserGroup>();
        groups.add(new UserGroup(2L, 3L, null));
        u.setGroups(groups);
    }
}
