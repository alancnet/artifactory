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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Date: 8/26/12
 * Time: 11:46 PM
 *
 * @author freds
 */
@Test
public class PermissionTargetTest {

    public void simplePermTargetTest() {
        PermissionTarget p = new PermissionTarget(1L, "p1", "", "");
        p.setRepoKeys(new HashSet<String>(1));
        assertEquals(p.getPermTargetId(), 1L);
        assertEquals(p.getName(), "p1");
        assertTrue(p.getIncludes().isEmpty());
        assertTrue(p.getExcludes().isEmpty());
        assertTrue(p.getRepoKeys().isEmpty());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noPermTargetIdTest() {
        new PermissionTarget(0L, "XXX", "", "");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void noPermTargetNameTest() {
        new PermissionTarget(1L, "", "", "");
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void noRepoKeysSetTest() {
        PermissionTarget p = new PermissionTarget(1L, "p1", "", "");
        p.getRepoKeys();
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void tooManyRepoKeysSetTest() {
        PermissionTarget p = new PermissionTarget(1L, "p1", "", "");
        p.setRepoKeys(new HashSet<String>(1));
        p.setRepoKeys(new HashSet<String>(1));
    }

}
