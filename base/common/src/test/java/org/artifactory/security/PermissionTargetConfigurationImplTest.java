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

import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Noam Y. Tenne
 */
public class PermissionTargetConfigurationImplTest {

    @Test
    public void testDefaultValues() throws Exception {
        PermissionTargetConfigurationImpl permissionTargetConfiguration = new PermissionTargetConfigurationImpl();
        assertEquals(permissionTargetConfiguration.getExcludesPattern(), "", "Unexpected default excludes pattern.");
        assertEquals(permissionTargetConfiguration.getIncludesPattern(), "**", "Unexpected default includes pattern.");
        assertNull(permissionTargetConfiguration.getName(), "Unexpected default name.");
        assertNull(permissionTargetConfiguration.getPrincipals(), "Unexpected default principals.");
        assertNull(permissionTargetConfiguration.getRepositories(), "Unexpected default repositories.");
    }

    @Test
    public void testSetters() throws Exception {
        PermissionTargetConfigurationImpl permissionTargetConfiguration = new PermissionTargetConfigurationImpl();
        permissionTargetConfiguration.setExcludesPattern("excludes");
        permissionTargetConfiguration.setIncludesPattern("includes");
        permissionTargetConfiguration.setName("name");

        PrincipalConfigurationImpl principalConfiguration = new PrincipalConfigurationImpl();
        permissionTargetConfiguration.setPrincipals(principalConfiguration);

        List<String> repositories = Lists.newArrayList("key1", "key2");
        permissionTargetConfiguration.setRepositories(repositories);

        assertEquals(permissionTargetConfiguration.getExcludesPattern(), "excludes", "Unexpected excludes.");
        assertEquals(permissionTargetConfiguration.getIncludesPattern(), "includes", "Unexpected includes.");
        assertEquals(permissionTargetConfiguration.getName(), "name", "Unexpected name.");
        assertEquals(permissionTargetConfiguration.getPrincipals(), principalConfiguration, "Unexpected principals.");
        assertEquals(permissionTargetConfiguration.getRepositories(), repositories, "Unexpected repositories.");
    }
}
