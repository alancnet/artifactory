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
public class MavenDependencyDeclarationProviderTest extends BaseDependencyDeclarationProviderTest {

    public MavenDependencyDeclarationProviderTest() {
        super(DependencyDeclarationProviderType.MAVEN, RepoLayoutUtils.MAVEN_2_DEFAULT);
    }

    public void testGetSyntaxType() {
        testGetSyntaxType(Syntax.xml);
    }

    public void testGetDependencyDeclarationWithMinimalParams() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0.jar",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0</version>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithArtifactRevision() {
        testDependencyDeclaration("org/bob/1.0-SNAPSHOT/bob-1.0-11111111.111111-1.jar",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0-11111111.111111-1</version>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithClassifier() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0-momo.jar",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0</version>\n" +
                        "    <classifier>momo</classifier>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithExt() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0.pom",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0</version>\n" +
                        "    <type>pom</type>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithArtifactRevisionAndClassifier() {
        testDependencyDeclaration("org/bob/1.0-SNAPSHOT/bob-1.0-11111111.111111-1-momo.jar",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0-11111111.111111-1</version>\n" +
                        "    <classifier>momo</classifier>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithArtifactRevisionAndExt() {
        testDependencyDeclaration("org/bob/1.0-SNAPSHOT/bob-1.0-11111111.111111-1.pom",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0-11111111.111111-1</version>\n" +
                        "    <type>pom</type>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithClassifierAndExt() {
        testDependencyDeclaration("org/bob/1.0/bob-1.0-momo.jar.sha1",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0</version>\n" +
                        "    <classifier>momo</classifier>\n" +
                        "    <type>jar.sha1</type>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithAllParams() {
        testDependencyDeclaration("org/bob/1.0-SNAPSHOT/bob-1.0-11111111.111111-1-momo.jar.sha1",
                "<dependency>\n" +
                        "    <groupId>org</groupId>\n" +
                        "    <artifactId>bob</artifactId>\n" +
                        "    <version>1.0-11111111.111111-1</version>\n" +
                        "    <classifier>momo</classifier>\n" +
                        "    <type>jar.sha1</type>\n" +
                        "</dependency>");
    }
}