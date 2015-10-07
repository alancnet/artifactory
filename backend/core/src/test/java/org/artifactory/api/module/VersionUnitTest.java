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

import com.google.common.collect.Sets;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.RepoLayoutUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;

/**
 * Tests the Version Unit.
 *
 * @author Yossi Shaul
 */
@Test
public class VersionUnitTest {

    public void nodeConstructor() {
        RepoPath descriptorPath = InfoFactoryHolder.get()
                .createRepoPath("libs-releases", "org/artifactory/core/5.6/core-5.6.pom");
        Set<RepoPath> repoPaths = Sets.newHashSet(descriptorPath);
        ModuleInfo moduleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(descriptorPath.getPath(),
                RepoLayoutUtils.MAVEN_2_DEFAULT);
        VersionUnit vu = new VersionUnit(moduleInfo, repoPaths);

        assertEquals(vu.getRepoPaths(), repoPaths, "Unexpected repo path");
        assertEquals(vu.getModuleInfo().getOrganization(), "org.artifactory", "Unexpected group id");
        assertEquals(vu.getModuleInfo().getModule(), "core", "Unexpected artifact id");
        assertEquals(vu.getModuleInfo().getBaseRevision(), "5.6", "Unexpected version");

        RepoPath repoPath = vu.getRepoPaths().iterator().next();
        assertEquals(repoPath.getRepoKey(), "libs-releases", "Unexpected repoKey");
        assertEquals(repoPath.getPath(), "org/artifactory/core/5.6/core-5.6.pom", "Unexpected repo path");
    }

    public void invalidPath() {
        RepoPath repoPath = InfoFactoryHolder.get()
                .createRepoPath("libs-releases", "core/5.6");
        Set<RepoPath> repoPaths = Sets.newHashSet(repoPath);
        ModuleInfo moduleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(repoPath.getPath(),
                RepoLayoutUtils.MAVEN_2_DEFAULT);
        VersionUnit versionUnit = new VersionUnit(moduleInfo, repoPaths);
        ModuleInfo vUModuleInfo = versionUnit.getModuleInfo();
        Assert.assertNotNull(vUModuleInfo,
                "Version unit module info object should not be null even though it failed to match.");
        Assert.assertFalse(vUModuleInfo.isValid(), "Version unit module info object should not be valid.");
    }
}
