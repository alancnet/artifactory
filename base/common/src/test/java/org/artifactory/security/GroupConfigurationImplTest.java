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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
public class GroupConfigurationImplTest {

    @Test
    public void testDefaultConstructor() throws Exception {
        GroupConfigurationImpl groupConfiguration = new GroupConfigurationImpl();
        assertNull(groupConfiguration.getRealm(), "Unexpected default realm.");
        assertNull(groupConfiguration.getDescription(), "Unexpected default description.");
        assertNull(groupConfiguration.getName(), "Unexpected default name.");
        assertFalse(groupConfiguration.isAutoJoin(), "Unexpected default auto join state.");
    }

    @Test
    public void testSetters() throws Exception {
        GroupConfigurationImpl groupConfiguration = new GroupConfigurationImpl();
        groupConfiguration.setDescription("desc");
        groupConfiguration.setName("name");
        groupConfiguration.setAutoJoin(true);
        groupConfiguration.setRealm("realm");

        assertEquals(groupConfiguration.getRealm(), "realm", "Unexpected realm.");
        assertEquals(groupConfiguration.getDescription(), "desc", "Unexpected description.");
        assertEquals(groupConfiguration.getName(), "name", "Unexpected name.");
        assertTrue(groupConfiguration.isAutoJoin(), "Unexpected auto join state.");
    }
}
