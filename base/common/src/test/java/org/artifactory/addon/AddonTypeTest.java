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

package org.artifactory.addon;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * User: Shay Yaakov
 */
@Test
public class AddonTypeTest {

    @Test
    public void testAddonsDisplayOrdinal() {
        assertEquals(AddonType.AOL.getDisplayOrdinal(), -1, "Unexpected ordinal value");
        assertEquals(AddonType.BUILD.getDisplayOrdinal(), 100, "Unexpected ordinal value");
        assertEquals(AddonType.LICENSES.getDisplayOrdinal(), 200, "Unexpected ordinal value");
        assertEquals(AddonType.REST.getDisplayOrdinal(), 300, "Unexpected ordinal value");
        assertEquals(AddonType.LDAP.getDisplayOrdinal(), 400, "Unexpected ordinal value");
        assertEquals(AddonType.REPLICATION.getDisplayOrdinal(), 500, "Unexpected ordinal value");
        assertEquals(AddonType.PROPERTIES.getDisplayOrdinal(), 600, "Unexpected ordinal value");
        assertEquals(AddonType.SEARCH.getDisplayOrdinal(), 700, "Unexpected ordinal value");
        assertEquals(AddonType.PLUGINS.getDisplayOrdinal(), 800, "Unexpected ordinal value");
        assertEquals(AddonType.YUM.getDisplayOrdinal(), 900, "Unexpected ordinal value");
        assertEquals(AddonType.P2.getDisplayOrdinal(), 1000, "Unexpected ordinal value");
        assertEquals(AddonType.NUGET.getDisplayOrdinal(), 1100, "Unexpected ordinal value");
        assertEquals(AddonType.LAYOUTS.getDisplayOrdinal(), 1200, "Unexpected ordinal value");
        assertEquals(AddonType.FILTERED_RESOURCES.getDisplayOrdinal(), 1300, "Unexpected ordinal value");
        assertEquals(AddonType.SSO.getDisplayOrdinal(), 1400, "Unexpected ordinal value");
        assertEquals(AddonType.WATCH.getDisplayOrdinal(), 1500, "Unexpected ordinal value");
        assertEquals(AddonType.WEBSTART.getDisplayOrdinal(), 1600, "Unexpected ordinal value");
    }
}
