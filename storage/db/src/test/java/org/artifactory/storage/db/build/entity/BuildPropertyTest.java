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

package org.artifactory.storage.db.build.entity;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Date: 11/23/12
 * Time: 10:41 AM
 *
 * @author freds
 */
@Test
public class BuildPropertyTest {
    public void simpleBuildPropertyTest() {
        BuildProperty bp = new BuildProperty(1L, 2L, "k", "v");
        assertEquals(bp.getPropId(), 1L);
        assertEquals(bp.getBuildId(), 2L);
        assertEquals(bp.getPropKey(), "k");
        assertEquals(bp.getPropValue(), "v");
    }

    public void maxNullsBuildPropertyTest() {
        BuildProperty bp = new BuildProperty(1L, 2L, "k", null);
        assertEquals(bp.getPropId(), 1L);
        assertEquals(bp.getBuildId(), 2L);
        assertEquals(bp.getPropKey(), "k");
        assertNull(bp.getPropValue());
    }

    public void equalsBuildPropertyTest() {
        BuildProperty bp1 = new BuildProperty(1L, 2L, "k", "v");
        BuildProperty bp2 = new BuildProperty(2L, 2L, "k", "v");
        BuildProperty bp2a = new BuildProperty(2L, 3L, "k", "v");
        BuildProperty bp2b = new BuildProperty(2L, 2L, "k1", "v");
        BuildProperty bp2c = new BuildProperty(2L, 2L, "k", "v1");
        BuildProperty bp3 = new BuildProperty(1L, 3L, "nk", null);
        assertNotEquals(bp1, bp2);
        assertNotEquals(bp1.hashCode(), bp2.hashCode());
        assertNotEquals(bp3, bp2);
        assertNotEquals(bp3.hashCode(), bp2.hashCode());
        assertEquals(bp1, bp3);
        assertEquals(bp1.hashCode(), bp3.hashCode());
        assertEquals(bp2, bp2a);
        assertEquals(bp2.hashCode(), bp2a.hashCode());
        assertEquals(bp2, bp2b);
        assertEquals(bp2.hashCode(), bp2b.hashCode());
        assertEquals(bp2, bp2c);
        assertEquals(bp2.hashCode(), bp2c.hashCode());
        assertTrue(bp1.isIdentical(bp2));
        assertFalse(bp2.isIdentical(bp2a));
        assertFalse(bp2.isIdentical(bp2b));
        assertFalse(bp2.isIdentical(bp2c));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*zero.*")
    public void noIdBuildPropertyTest() {
        new BuildProperty(0L, 2L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negIdBuildPropertyTest() {
        new BuildProperty(-1L, 2L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*zero.*")
    public void noBuildIdBuildPropertyTest() {
        new BuildProperty(1L, 0L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negBuildIdBuildPropertyTest() {
        new BuildProperty(1L, -2L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*key.*null.*")
    public void nullKeyBuildPropertyTest() {
        new BuildProperty(1L, 2L, null, "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*key.*empty.*")
    public void noKeyBuildPropertyTest() {
        new BuildProperty(1L, 2L, " ", "v");
    }


}
