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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Noam Y. Tenne
 */
public class PrincipalConfigurationImplTest {

    @Test
    public void testDefaultValues() throws Exception {
        PrincipalConfigurationImpl principalConfiguration = new PrincipalConfigurationImpl();
        assertNull(principalConfiguration.getUsers(), "Unexpected default users.");
        assertNull(principalConfiguration.getGroups(), "Unexpected default groups.");
    }

    @Test
    public void testSetters() throws Exception {
        Map<String, Set<String>> users = Maps.newHashMap();
        users.put("momo", Sets.<String>newHashSet("popo"));

        Map<String, Set<String>> groups = Maps.newHashMap();
        users.put("koko", Sets.<String>newHashSet("jojo"));

        PrincipalConfigurationImpl principalConfiguration = new PrincipalConfigurationImpl();
        principalConfiguration.setUsers(users);
        principalConfiguration.setGroups(groups);

        assertEquals(principalConfiguration.getUsers(), users, "Unexpected users.");
        assertEquals(principalConfiguration.getGroups(), groups, "Unexpected groups.");
    }
}
