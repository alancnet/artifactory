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
public class IvyDependencyDeclarationProviderTest extends BaseDependencyDeclarationProviderTest {

    public IvyDependencyDeclarationProviderTest() {
        super(DependencyDeclarationProviderType.IVY, RepoLayoutUtils.IVY_DEFAULT);
    }

    public void testGetSyntaxType() {
        testGetSyntaxType(Syntax.xml);
    }

    public void testGetDependencyDeclarationWithMinimalParams() {
        testDependencyDeclaration("org/bob/1.0/jars/bob-1.0.jar", "<dependency org=\"org\" name=\"bob\" rev=\"1.0\"/>");
    }

    public void testGetDependencyDeclarationWithArtifactRevision() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/jars/bob-1.0-11111111111111.jar",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0-11111111111111\"/>");
    }

    public void testGetDependencyDeclarationWithClassifier() {
        testDependencyDeclaration("org/bob/1.0/jars/bob-momo-1.0.jar",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0\">\n" +
                        "    <artifact name=\"bob\" type=\"jar\" m:classifier=\"momo\" ext=\"jar\"/>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithExt() {
        testDependencyDeclaration("org/bob/1.0/xmls/ivy-1.0.xml",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0\">\n" +
                        "    <artifact name=\"bob\" type=\"xml\" ext=\"xml\"/>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithArtifactRevisionAndClassifier() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/jars/bob-momo-1.0-11111111111111.jar",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0-11111111111111\">\n" +
                        "    <artifact name=\"bob\" type=\"jar\" m:classifier=\"momo\" ext=\"jar\"/>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithArtifactRevisionAndExt() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/xmls/ivy-1.0-11111111111111.xml",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0-11111111111111\">\n" +
                        "    <artifact name=\"bob\" type=\"xml\" ext=\"xml\"/>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithClassifierAndExt() {
        testDependencyDeclaration("org/bob/1.0/tar.gzs/bob-momo-1.0.tar.gz",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0\">\n" +
                        "    <artifact name=\"bob\" type=\"tar.gz\" m:classifier=\"momo\" ext=\"tar.gz\"/>\n" +
                        "</dependency>");
    }

    public void testGetDependencyDeclarationWithAllParams() {
        testDependencyDeclaration("org/bob/1.0-11111111111111/tar.gzs/bob-momo-1.0-11111111111111.tar.gz",
                "<dependency org=\"org\" name=\"bob\" rev=\"1.0-11111111111111\">\n" +
                        "    <artifact name=\"bob\" type=\"tar.gz\" m:classifier=\"momo\" ext=\"tar.gz\"/>\n" +
                        "</dependency>");
    }
}