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

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Noam Y. Tenne
 */
@Test
public class DependencyDeclarationProviderTypeTest {

    public void testDefaultValues() {
        checkDefaultValues(DependencyDeclarationProviderType.MAVEN, "Maven", MavenDependencyDeclarationProvider.class);

        checkDefaultValues(DependencyDeclarationProviderType.IVY, "Ivy", IvyDependencyDeclarationProvider.class);

        checkDefaultValues(DependencyDeclarationProviderType.GRADLE, "Gradle",
                GradleDependencyDeclarationProvider.class);
    }

    private void checkDefaultValues(DependencyDeclarationProviderType type, String displayName,
            Class<? extends DependencyDeclarationProvider> providerType) {
        assertEquals(type.getDisplayName(), displayName, "Unexpected default provider type name.");

        Assert.assertTrue(type.getDeclarationProvider().getClass().isAssignableFrom(providerType),
                "Unexpected default provider.");
    }
}
