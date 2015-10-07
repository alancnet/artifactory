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

package org.artifactory.repo;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests the RepoDetailsType enum
 *
 * @author Noam Y. Tenne
 */
@Test
public class RepoDetailsTypeTest {

    /**
     * Tests the different type display names
     */
    public void testDisplayName() {
        assertEquals(RepoDetailsType.LOCAL.getTypeName(), "Local");
        assertEquals(RepoDetailsType.REMOTE.getTypeName(), "Remote");
        assertEquals(RepoDetailsType.VIRTUAL.getTypeName(), "Virtual");
    }
}