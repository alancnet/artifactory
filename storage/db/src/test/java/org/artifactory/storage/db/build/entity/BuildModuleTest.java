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

import java.util.HashSet;

import static org.testng.Assert.*;

/**
 * Date: 10/31/12
 * Time: 8:41 AM
 *
 * @author freds
 */
@Test
public class BuildModuleTest {

    public void simpleBuildModuleTest() {
        BuildModule bm = new BuildModule(1L, 2L, "a");
        bm.setProperties(new HashSet<ModuleProperty>());
        assertEquals(bm.getModuleId(), 1L);
        assertEquals(bm.getBuildId(), 2L);
        assertEquals(bm.getModuleNameId(), "a");
        assertTrue(bm.getProperties().isEmpty());
    }

    public void simpleModulePropertyTest() {
        ModuleProperty mp = new ModuleProperty(1L, 2L, "k", "v");
        assertEquals(mp.getPropId(), 1L);
        assertEquals(mp.getModuleId(), 2L);
        assertEquals(mp.getPropKey(), "k");
        assertEquals(mp.getPropValue(), "v");
    }

    public void maxNullsModulePropertyTest() {
        ModuleProperty mp = new ModuleProperty(1L, 2L, "k", null);
        assertEquals(mp.getPropId(), 1L);
        assertEquals(mp.getModuleId(), 2L);
        assertEquals(mp.getPropKey(), "k");
        assertNull(mp.getPropValue());
    }

    public void equalsModulePropertyTest() {
        ModuleProperty mp1 = new ModuleProperty(1L, 2L, "k", "v");
        ModuleProperty mp2 = new ModuleProperty(2L, 2L, "k", "v");
        ModuleProperty mp2a = new ModuleProperty(2L, 3L, "k", "v");
        ModuleProperty mp2b = new ModuleProperty(2L, 2L, "k1", "v");
        ModuleProperty mp2c = new ModuleProperty(2L, 2L, "k", "v1");
        ModuleProperty mp3 = new ModuleProperty(1L, 3L, "nk", null);
        assertNotEquals(mp1, mp2);
        assertNotEquals(mp1.hashCode(), mp2.hashCode());
        assertNotEquals(mp3, mp2);
        assertNotEquals(mp3.hashCode(), mp2.hashCode());
        assertEquals(mp1, mp3);
        assertEquals(mp1.hashCode(), mp3.hashCode());
        assertEquals(mp2, mp2a);
        assertEquals(mp2.hashCode(), mp2a.hashCode());
        assertEquals(mp2, mp2b);
        assertEquals(mp2.hashCode(), mp2b.hashCode());
        assertEquals(mp2, mp2c);
        assertEquals(mp2.hashCode(), mp2c.hashCode());
        assertTrue(mp1.isIdentical(mp2));
        assertFalse(mp2.isIdentical(mp2a));
        assertFalse(mp2.isIdentical(mp2b));
        assertFalse(mp2.isIdentical(mp2c));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noIdModulePropertyTest() {
        new ModuleProperty(0L, 2L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negIdModulePropertyTest() {
        new ModuleProperty(-1L, 2L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noBuildIdModulePropertyTest() {
        new ModuleProperty(1L, 0L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negBuildIdModulePropertyTest() {
        new ModuleProperty(1L, -2L, "k", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*key.*null.*")
    public void nullKeyModulePropertyTest() {
        new ModuleProperty(1L, 2L, null, "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*key.*null.*")
    public void noKeyModulePropertyTest() {
        new ModuleProperty(1L, 2L, " ", "v");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noIdBuildModuleTest() {
        new BuildModule(0L, 2L, "a");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negIdBuildModuleTest() {
        new BuildModule(-1L, 2L, "a");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noBuildIdBuildModuleTest() {
        new BuildModule(1L, 0L, "a");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negBuildIdBuildModuleTest() {
        new BuildModule(1L, -2L, "a");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void nullNameBuildModuleTest() {
        new BuildModule(1L, 2L, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void noNameBuildModuleTest() {
        new BuildModule(1L, 2L, " ");
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*not initialized.*Properties missing.*")
    public void noPropertiesInBuildModuleTest() {
        BuildModule bm = new BuildModule(1L, 2L, "a");
        bm.getProperties();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*set properties.*null.*")
    public void setNullPropertiesInBuildModuleTest() {
        BuildModule bm = new BuildModule(1L, 2L, "a");
        bm.setProperties(null);
    }

    @Test(expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = ".*Properties already set.*")
    public void doubleSetPropertiesInBuildModuleTest() {
        BuildModule bm = new BuildModule(1L, 2L, "a");
        bm.setProperties(new HashSet<ModuleProperty>());
        bm.setProperties(new HashSet<ModuleProperty>());
    }

}
