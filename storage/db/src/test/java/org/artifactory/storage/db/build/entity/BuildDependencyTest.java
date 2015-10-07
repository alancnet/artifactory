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

import java.util.Set;

import static org.testng.Assert.*;

/**
 * Date: 10/30/12
 * Time: 2:02 PM
 *
 * @author freds
 */
@Test
public class BuildDependencyTest {

    public void simpleBuildDependencyTest() {
        BuildDependency b = new BuildDependency(1L, 2L, "b", "c,d", "t", "a1", "a2");
        assertEquals(b.getDependencyId(), 1L);
        assertEquals(b.getModuleId(), 2L);
        assertEquals(b.getDependencyNameId(), "b");
        assertEquals(b.getDependencyScopes(), "c,d");
        assertEquals(b.getDependencyType(), "t");
        assertEquals(b.getSha1(), "a1");
        assertEquals(b.getMd5(), "a2");
    }

    public void mostlyNullBuildDependencyTest() {
        BuildDependency b = new BuildDependency(1L, 2L, "b", "", null, null, null);
        assertEquals(b.getDependencyId(), 1L);
        assertEquals(b.getModuleId(), 2L);
        assertEquals(b.getDependencyNameId(), "b");
        assertEquals(b.getDependencyScopes(), "");
        assertTrue(b.getScopes().isEmpty());
        assertNull(b.getDependencyType());
        assertNull(b.getSha1());
        assertNull(b.getMd5());
    }

    public void mostlyNullBuildDependencyTest2() {
        BuildDependency b = new BuildDependency(1L, 2L, "b", (String) null, null, null, null);
        assertEquals(b.getDependencyId(), 1L);
        assertEquals(b.getModuleId(), 2L);
        assertEquals(b.getDependencyNameId(), "b");
        assertNull(b.getDependencyScopes());
        assertTrue(b.getScopes().isEmpty());
        assertNull(b.getDependencyType());
        assertNull(b.getSha1());
        assertNull(b.getMd5());
    }

    public void mostlyNullBuildDependencyTest3() {
        BuildDependency b = new BuildDependency(1L, 2L, "b", (Set<String>) null, null, null, null);
        assertEquals(b.getDependencyId(), 1L);
        assertEquals(b.getModuleId(), 2L);
        assertEquals(b.getDependencyNameId(), "b");
        assertEquals(b.getDependencyScopes(), "");
        assertTrue(b.getScopes().isEmpty());
        assertNull(b.getDependencyType());
        assertNull(b.getSha1());
        assertNull(b.getMd5());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noDependencyIdTest() {
        new BuildDependency(0L, 2L, "b", "", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negDependencyIdTest() {
        new BuildDependency(-2L, 2L, "b", "", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noModuleIdTest() {
        new BuildDependency(1L, 0L, "b", "", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negModuleIdTest() {
        new BuildDependency(1L, -2L, "b", "", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void noDependencyNameTest() {
        new BuildDependency(1L, 2L, null, (String) null, null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void emptyDependencyNameTest() {
        new BuildDependency(1L, 2L, " ", (String) null, null, null, null);
    }
}
