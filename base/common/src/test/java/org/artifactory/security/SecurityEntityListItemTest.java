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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Noam Y. Tenne
 */
public class SecurityEntityListItemTest {

    @Test
    public void testDefaultValues() throws Exception {
        SecurityEntityListItem securityEntityListItem = new SecurityEntityListItem();
        assertNull(securityEntityListItem.getName(), "Unexpected default entity name.");
        assertNull(securityEntityListItem.getUri(), "Unexpected default entity URI.");
    }

    @Test
    public void testSetters() throws Exception {
        SecurityEntityListItem securityEntityListItem = new SecurityEntityListItem();
        securityEntityListItem.setName("name");
        securityEntityListItem.setUri("uri");
        assertEquals(securityEntityListItem.getName(), "name", "Unexpected entity name.");
        assertEquals(securityEntityListItem.getUri(), "uri", "Unexpected entity URI.");
    }
}
