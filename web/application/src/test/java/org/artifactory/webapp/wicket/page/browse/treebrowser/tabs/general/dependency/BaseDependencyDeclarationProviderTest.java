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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.dependency;

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.descriptor.repo.RepoLayout;
import org.testng.Assert;

import static org.testng.Assert.assertEquals;

/**
 * @author Noam Y. Tenne
 */
public abstract class BaseDependencyDeclarationProviderTest {

    private final RepoLayout repoLayout;
    private final DependencyDeclarationProvider provider;

    protected BaseDependencyDeclarationProviderTest(DependencyDeclarationProviderType type, RepoLayout repoLayout) {
        this.repoLayout = repoLayout;
        provider = type.getDeclarationProvider();
    }

    protected void testGetSyntaxType(Syntax expectedSyntaxType) {
        assertEquals(provider.getSyntaxType(), expectedSyntaxType, "Unexpected provider syntax.");
    }

    protected void testDependencyDeclaration(String path, String expectedDeclaration) {
        ModuleInfo moduleInfo = null;
        if (repoLayout.isDistinctiveDescriptorPathPattern()) {
            moduleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(path, repoLayout);
        }

        if ((moduleInfo == null) || !moduleInfo.isValid()) {
            moduleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(path, repoLayout);
        }

        Assert.assertTrue(moduleInfo.isValid(), "Unable to create a valid module info object.");
        String declaration = provider.getDependencyDeclaration(moduleInfo);
        assertEquals(declaration, expectedDeclaration, "Unexpected dependency declaration was generated.");
    }
}
