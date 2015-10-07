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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Date: 10/30/12
 * Time: 2:02 PM
 *
 * @author freds
 */
@Test
public class BuildArtifactTest {

    public void simpleBuildArtifactTest() {
        BuildArtifact b = new BuildArtifact(1L, 2L, "b", "t", "a1", "a2");
        assertEquals(b.getArtifactId(), 1L);
        assertEquals(b.getModuleId(), 2L);
        assertEquals(b.getArtifactName(), "b");
        assertEquals(b.getArtifactType(), "t");
        assertEquals(b.getSha1(), "a1");
        assertEquals(b.getMd5(), "a2");
    }

    public void mostlyNullBuildArtifactTest() {
        BuildArtifact b = new BuildArtifact(1L, 2L, "b", null, null, null);
        assertEquals(b.getArtifactId(), 1L);
        assertEquals(b.getModuleId(), 2L);
        assertEquals(b.getArtifactName(), "b");
        assertNull(b.getArtifactType());
        assertNull(b.getSha1());
        assertNull(b.getMd5());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noArtifactIdTest() {
        new BuildArtifact(0L, 2L, "b", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negArtifactIdTest() {
        new BuildArtifact(-2L, 2L, "b", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void noModuleIdTest() {
        new BuildArtifact(1L, 0L, "b", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*id.*negative.*")
    public void negModuleIdTest() {
        new BuildArtifact(1L, -2L, "b", null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void noArtifactNameTest() {
        new BuildArtifact(1L, 2L, null, null, null, null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*name.*null.*")
    public void emptyArtifactNameTest() {
        new BuildArtifact(1L, 2L, " ", null, null, null);
    }
}
