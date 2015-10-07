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

import org.artifactory.descriptor.repo.RepoLayout;

import static org.testng.Assert.assertEquals;

/**
 * @author Noam Y. Tenne
 */
public abstract class BaseModuleInfoUtilsTest {

    protected RepoLayout repoLayout;

    protected BaseModuleInfoUtilsTest(RepoLayout repoLayout) {
        this.repoLayout = repoLayout;
    }

    protected void testArtifactModuleToModule(ModuleInfo moduleInfo) {
        testArtifactModuleToModule(moduleInfo, repoLayout);
    }

    protected void testArtifactModuleToModule(ModuleInfo moduleInfo, RepoLayout layoutToTest) {
        String artifactPath = ModuleInfoUtils.constructArtifactPath(moduleInfo, layoutToTest);
        ModuleInfo extractedModuleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(artifactPath, layoutToTest);
        assertEquals(extractedModuleInfo, moduleInfo, "Converted module info does not match the original one.");
    }

    protected void testArtifactPathToModule(String artifactPath, ModuleInfo expectedModule) {
        testArtifactPathToModule(artifactPath, expectedModule, repoLayout);
    }

    protected void testArtifactPathToModule(String artifactPath, ModuleInfo expectedModule, RepoLayout layoutToTest) {
        ModuleInfo extractedModuleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(artifactPath, layoutToTest);
        assertEquals(extractedModuleInfo, expectedModule, "Converted module info does not match the expected one.");
    }

    protected void testDescriptorPathToModule(String descriptorPath, ModuleInfo expectedModule) {
        testDescriptorPathToModule(descriptorPath, expectedModule, repoLayout);
    }

    protected void testDescriptorPathToModule(String descriptorPath, ModuleInfo expectedModule, RepoLayout
            layoutToTest) {
        ModuleInfo extractedModuleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(descriptorPath, layoutToTest);
        assertEquals(extractedModuleInfo, expectedModule, "Converted module info does not match the expected one.");
    }
}
