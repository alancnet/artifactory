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

import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.util.RepoLayoutUtils;
import org.testng.annotations.Test;

/**
 * @author Noam Y. Tenne
 */
@Test
public class GradleDependencyDeclarationProviderTest extends BaseDependencyDeclarationProviderTest {

    public GradleDependencyDeclarationProviderTest() {
        super(DependencyDeclarationProviderType.GRADLE, RepoLayoutUtils.GRADLE_DEFAULT);
    }

    public void testGetSyntaxType() {
        testGetSyntaxType(Syntax.groovy);
    }

    public void testGetDependencyDeclarationWithMinimalParams() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0.jar", "compile(group: 'org', name: 'bob', version: '1.0')");
    }

    public void testGetDependencyDeclarationWithArtifactRevision() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/bob-1.0-11111111111111.jar",
                "compile(group: 'org', name: 'bob', version: '1.0-11111111111111')");
    }

    public void testGetDependencyDeclarationWithClassifier() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0-momo.jar",
                "compile(group: 'org', name: 'bob', version: '1.0', classifier: 'momo')");
    }

    public void testGetDependencyDeclarationWithExt() {
        testDependencyDeclaration("org/bob/ivy-1.0.xml",
                "compile(group: 'org', name: 'bob', version: '1.0', ext: 'xml')");
    }

    public void testGetDependencyDeclarationWithArtifactRevisionAndClassifier() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/bob-1.0-11111111111111-momo.jar",
                "compile(group: 'org', name: 'bob', version: '1.0-11111111111111', classifier: 'momo')");
    }

    public void testGetDependencyDeclarationWithArtifactRevisionAndExt() {
        testDependencyDeclaration("org/bob/ivy-1.0-11111111111111.xml",
                "compile(group: 'org', name: 'bob', version: '1.0-11111111111111', ext: 'xml')");
    }

    public void testGetDependencyDeclarationWithClassifierAndExt() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0-momo.jar.sha1",
                "compile(group: 'org', name: 'bob', version: '1.0', classifier: 'momo', ext: 'jar.sha1')");
    }

    public void testGetDependencyDeclarationWithAllParams() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/bob-1.0-11111111111111-momo.jar.sha1",
                "compile(group: 'org', name: 'bob', version: '1.0-11111111111111', " +
                        "classifier: 'momo', ext: 'jar.sha1')");
    }
}