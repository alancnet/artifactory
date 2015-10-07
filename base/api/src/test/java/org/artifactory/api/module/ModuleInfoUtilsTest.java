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

package org.artifactory.api.module;

import org.testng.annotations.Test;

import static org.artifactory.api.module.ModuleInfoUtils.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class ModuleInfoUtilsTest {

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Unable to construct a path from a null module info object.")
    public void constructArtifactPathWithNullModuleInfo() {
        constructArtifactPath(null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Unable to construct a path from an invalid module info object.")
    public void constructArtifactPathWithInvalidModuleInfo() {
        constructArtifactPath(new ModuleInfo(), null);
    }

    /*@Test(description = "RTFACT-7125")
    public void constructMavenSnapshotWithDots() {
        ModuleInfo module = new ModuleInfoBuilder().organization("org.jfrog").module("mod").baseRevision("1.0").ext("jar")
                .fileIntegrationRevision("SNAPSHOT").folderIntegrationRevision("SNAPSHOT").build();
        String path = constructArtifactPath(module, RepoLayoutUtils.MAVEN_2_DEFAULT, true, true);
        Assert.assertEquals(path, "org.jfrog/mod/1.0-SNAPSHOT/mod-1.0-SNAPSHOT.jar");
        String path2 = constructArtifactPath(module, RepoLayoutUtils.MAVEN_2_DEFAULT);
        Assert.assertEquals(path2, "org/jfrog/mod/1.0-SNAPSHOT/mod-1.0-SNAPSHOT.jar");
        String path3 = constructArtifactPath(module, RepoLayoutUtils.MAVEN_2_DEFAULT, true);
        Assert.assertEquals(path3, "org/jfrog/mod/1.0-SNAPSHOT/mod-1.0-SNAPSHOT.jar");
    }*/

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Unable to construct a path from a null repository layout.")
    public void constructArtifactPathWithNullRepoLayout() {
        constructArtifactPath(new ModuleInfoBuilder().organization("org").module("mod").baseRevision("rev").build(),
                null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Cannot construct a module info object from a blank item path.")
    public void moduleInfoFromArtifactPathWithNullArtifactPath() {
        moduleInfoFromArtifactPath(null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Cannot construct a module info object from a blank item path.")
    public void moduleInfoFromDescriptorPathWithNullArtifactPath() {
        moduleInfoFromDescriptorPath(null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Cannot construct a module info object from a blank item path.")
    public void moduleInfoFromArtifactPathWithBlankItemPath() {
        moduleInfoFromArtifactPath("", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Cannot construct a module info object from a blank item path.")
    public void moduleInfoFromDescriptorPathWithBlankItemPath() {
        moduleInfoFromDescriptorPath("", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Cannot construct a module info object from a null repository layout.")
    public void moduleInfoFromArtifactPathWithNullRepoLayout() {
        moduleInfoFromArtifactPath("org/meow", null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Cannot construct a module info object from a null repository layout.")
    public void moduleInfoFromDescriptorPathWithNullRepoLayout() {
        moduleInfoFromDescriptorPath("org/meow", null);
    }
}
