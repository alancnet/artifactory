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
import org.artifactory.api.security.SecurityService;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.security.dao.UserGroupsDao;
import org.artifactory.storage.db.security.entity.Group;
import org.artifactory.storage.db.security.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.testng.Assert.*;

/**
 * Date: 11/13/12
 * Time: 4:27 PM
 *
 * @author freds
 */
public abstract class SecurityBaseDaoTest extends DbBaseTest {

    @Autowired
    protected UserGroupsDao userGroupsDao;

    protected void checkUserCollection(Collection<User> users, ImmutableSet<Integer> expectedUids) {
        checkUserCollection(users, expectedUids, 0);
    }

    protected void checkUserCollection(Collection<User> users, ImmutableSet<Integer> expectedUids, int nbGroupsDelta) {
        assertEquals(users.size(), expectedUids.size());
        for (User u : users) {
            int gid = (int) u.getUserId();
            assertTrue(expectedUids.contains(gid));
            assertAnyUser(u, gid, nbGroupsDelta);
        }
    }

    private void assertAnyUser(User user, int uid, int nbGroupsDelta) {
        switch (uid) {
            case 1:
                assertUser1(user, nbGroupsDelta);
                break;
            case 2:
                assertUser2(user, nbGroupsDelta);
                break;
            case 3:
                assertUser3(user, nbGroupsDelta);
                break;
            case 15:
                assertUser15(user, nbGroupsDelta);
                break;
            case 16:
                assertUser16(user, nbGroupsDelta);
                break;
            default:
                fail("User " + user + " unknown!");
        }
    }

    protected void assertUser1(User u, int nbGroupsDelta) {
        assertEquals(u.getUserId(), 1L);
        assertEquals(u.getUsername(), "u1");
        assertEquals(u.getPassword(), "apass");
        assertEquals(u.getEmail(), "e@mail.com");
        assertFalse(u.isAdmin());
        assertTrue(u.isEnabled());
        assertFalse(u.isUpdatableProfile());
        assertEquals(u.getGroups().size(), 2 + nbGroupsDelta);
    }

    protected void assertUser2(User u, int nbGroupsDelta) {
        assertEquals(u.getUserId(), 2L);
        assertEquals(u.getUsername(), "u2");
        assertEquals(u.getPassword(), "bpass");
        assertEquals(u.getEmail(), "f@mail.com");
        assertTrue(u.isAdmin());
        assertTrue(u.isEnabled());
        assertTrue(u.isUpdatableProfile());
        assertEquals(u.getGroups().size(), 1 + nbGroupsDelta);
    }

    protected void assertUser3(User u, int nbGroupsDelta) {
        assertEquals(u.getUserId(), 3L);
        assertEquals(u.getUsername(), "u3");
        assertEquals(u.getPassword(), "");
        assertTrue(u.isAdmin());
        assertFalse(u.isEnabled());
        assertTrue(u.isUpdatableProfile());
        assertEquals(u.getGroups().size(), 0 + nbGroupsDelta);
        assertNull(u.getSalt());
        assertNull(u.getEmail());
        assertNull(u.getGenPasswordKey());
        assertNull(u.getRealm());
        assertNull(u.getPrivateKey());
        assertNull(u.getPublicKey());
        assertNull(u.getLastLoginClientIp());
        assertNull(u.getLastAccessClientIp());
        assertNull(u.getBintrayAuth());
    }

    private void assertUser15(User u, int nbGroupsDelta) {
        assertEquals(u.getUserId(), 15L);
        assertEquals(u.getUsername(), UserInfo.ANONYMOUS);
        assertEquals(u.getPassword(), MutableUserInfo.INVALID_PASSWORD);
        assertNull(u.getEmail());
        assertFalse(u.isAdmin());
        assertTrue(u.isEnabled());
        assertFalse(u.isUpdatableProfile());
        assertEquals(u.getGroups().size(), 2 + nbGroupsDelta);
    }

    private void assertUser16(User u, int nbGroupsDelta) {
        assertEquals(u.getUserId(), 16L);
        assertEquals(u.getUsername(), SecurityService.DEFAULT_ADMIN_USER);
        assertEquals(u.getPassword(), "password");
        assertNull(u.getEmail());
        assertTrue(u.isAdmin());
        assertTrue(u.isEnabled());
        assertTrue(u.isUpdatableProfile());
        assertEquals(u.getGroups().size(), 0 + nbGroupsDelta);
    }

    protected void checkGroupCollection(Collection<Group> groups, ImmutableSet<Integer> expectedGids) {
        assertEquals(groups.size(), expectedGids.size());
        for (Group group : groups) {
            int gid = (int) group.getGroupId();
            assertTrue(expectedGids.contains(gid));
            assertAnyGroup(group, gid);
        }
    }

    private void assertAnyGroup(Group defaultGroup, int gid) {
        switch (gid) {
            case 1:
                assertGroup1(defaultGroup);
                break;
            case 2:
                assertGroup2(defaultGroup);
                break;
            case 3:
                assertGroup3(defaultGroup);
                break;
            case 15:
                assertGroup15(defaultGroup);
                break;
            default:
                fail("Default group " + defaultGroup + " unknown!");
        }
    }

    protected void assertGroup1(Group g) {
        assertEquals(g.getGroupId(), 1L);
        assertEquals(g.getGroupName(), "g1");
        assertNull(g.getDescription());
        assertFalse(g.isNewUserDefault());
        assertNull(g.getRealm());
        assertNull(g.getRealmAttributes());
    }

    protected void assertGroup2(Group g) {
        assertEquals(g.getGroupId(), 2L);
        assertEquals(g.getGroupName(), "g2");
        assertEquals(g.getDescription(), "is default");
        assertTrue(g.isNewUserDefault());
        assertEquals(g.getRealm(), "default realm");
        assertEquals(g.getRealmAttributes(), "default att");
    }

    private void assertGroup3(Group g) {
        assertEquals(g.getGroupId(), 3L);
        assertEquals(g.getGroupName(), "g3");
        assertEquals(g.getDescription(), "no one");
        assertFalse(g.isNewUserDefault());
        assertEquals(g.getRealm(), SecurityConstants.DEFAULT_REALM);
        assertNull(g.getRealmAttributes());
    }

    private void assertGroup15(Group g) {
        assertEquals(g.getGroupId(), 15L);
        assertEquals(g.getGroupName(), "readers");
        assertEquals(g.getDescription(), "readers");
        assertTrue(g.isNewUserDefault());
        assertEquals(g.getRealm(), SecurityConstants.DEFAULT_REALM);
        assertNull(g.getRealmAttributes());
    }
}
