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

package org.artifactory.security;

import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
public class UserConfigurationImplTest {

    @Test
    public void testDefaultConstructor() throws Exception {
        UserConfigurationImpl userConfiguration = new UserConfigurationImpl();
        assertNull(userConfiguration.getEmail(), "Unexpected default email.");
        assertNull(userConfiguration.getPassword(), "Unexpected default password.");
        assertNull(userConfiguration.getName(), "Unexpected default username.");
        assertFalse(userConfiguration.isAdmin(), "Unexpected default admin state.");
        assertTrue(userConfiguration.isProfileUpdatable(), "Unexpected default updatable profile state.");
        assertFalse(userConfiguration.isInternalPasswordDisabled(),
                "Unexpected default internal password disabled state.");
        assertNull(userConfiguration.getGroups(), "Unexpected default groups.");
        assertNull(userConfiguration.getLastLoggedIn(), "Unexpected default last logged in.");
        assertNull(userConfiguration.getRealm(), "Unexpected default realm.");
    }

    @Test
    public void testSetters() throws Exception {
        UserConfigurationImpl userConfiguration = new UserConfigurationImpl();
        userConfiguration.setEmail("email");
        userConfiguration.setPassword("password");
        userConfiguration.setName("username");
        userConfiguration.setAdmin(true);
        userConfiguration.setProfileUpdatable(true);
        userConfiguration.setInternalPasswordDisabled(true);

        Set<String> groups = Sets.newHashSet("group1", "group2");
        userConfiguration.setGroups(groups);
        userConfiguration.setLastLoggedIn("asfafdasfd");
        userConfiguration.setRealm("realm");

        assertEquals(userConfiguration.getEmail(), "email", "Unexpected email.");
        assertEquals(userConfiguration.getPassword(), "password", "Unexpected password.");
        assertEquals(userConfiguration.getName(), "username", "Unexpected username.");
        assertTrue(userConfiguration.isAdmin(), "Unexpected admin state.");
        assertTrue(userConfiguration.isProfileUpdatable(), "Unexpected updatable profile state.");
        assertTrue(userConfiguration.isInternalPasswordDisabled(), "Unexpected internal password disabled state.");
        assertEquals(userConfiguration.getGroups(), groups, "Unexpected groups.");
        assertEquals(userConfiguration.getLastLoggedIn(), "asfafdasfd", "Unexpected last logged in.");
        assertEquals(userConfiguration.getRealm(), "realm", "Unexpected realm.");
    }
}
