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

package org.artifactory.version.converter.v147;

import org.apache.commons.lang.StringUtils;
import org.artifactory.convert.XmlConverterTest;
import org.artifactory.util.RepoLayoutUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Noam Y. Tenne
 */
@Test
public class DefaultRepoLayoutConverterTest extends XmlConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.1.4.6_wrong_url.xml", new DefaultRepoLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);

        Element localRepositoriesElement = rootElement.getChild("localRepositories", namespace);
        if (localRepositoriesElement != null) {
            List<Element> localRepositoryElements = localRepositoriesElement.getChildren("localRepository", namespace);
            if (localRepositoryElements != null && !localRepositoryElements.isEmpty()) {

                for (Element localRepositoryElement : localRepositoryElements) {
                    checkRepoHasDefaultLayoutAndNoType(localRepositoryElement, namespace);
                }
            }
        }

        Element remoteRepositoriesElement = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepositoriesElement != null) {
            List<Element> remoteRepositoryElements =
                    remoteRepositoriesElement.getChildren("remoteRepository", namespace);
            if (remoteRepositoryElements != null && !remoteRepositoryElements.isEmpty()) {

                for (Element remoteRepositoryElement : remoteRepositoryElements) {
                    checkRepoHasDefaultLayoutAndNoType(remoteRepositoryElement, namespace);
                }
            }
        }

        Element virtualRepositoriesElement = rootElement.getChild("virtualRepositories", namespace);
        if (virtualRepositoriesElement != null) {
            List<Element> virtualRepositoryElements =
                    virtualRepositoriesElement.getChildren("virtualRepository", namespace);
            if (virtualRepositoryElements != null && !virtualRepositoryElements.isEmpty()) {

                for (Element virtualRepositoryElement : virtualRepositoryElements) {
                    String repoKey = assertAndGetRepoKey(virtualRepositoryElement, namespace);
                    assertNoType(repoKey, virtualRepositoryElement);
                }
            }
        }
    }

    private void checkRepoHasDefaultLayoutAndNoType(Element repositoryElement, Namespace namespace) {
        String repoKey = assertAndGetRepoKey(repositoryElement, namespace);

        Element repoLayoutRefElement = repositoryElement.getChild("repoLayoutRef", namespace);
        assertNotNull(repoLayoutRefElement, "Couldn't find a repository layout reference in: " + repoKey);

        String repoLayout = repoLayoutRefElement.getText();
        assertEquals(repoLayout, RepoLayoutUtils.MAVEN_2_DEFAULT_NAME, "Unexpected default repo layout reference.");

        assertNoType(repoKey, repositoryElement);

        if ("java.net.m1".equals(repoKey)) {
            Element remoteRepoLayoutRefElement = repositoryElement.getChild("remoteRepoLayoutRef", namespace);
            assertNotNull(remoteRepoLayoutRefElement, "Couldn't find a remote repository layout reference in: " +
                    repoKey);

            String remoteRepoLayout = remoteRepoLayoutRefElement.getText();
            assertEquals(remoteRepoLayout, RepoLayoutUtils.MAVEN_1_DEFAULT_NAME,
                    "Unexpected default remote repo layout reference.");
        }
    }

    private String assertAndGetRepoKey(Element repositoryElement, Namespace namespace) {
        Element repoKeyElement = repositoryElement.getChild("key", namespace);
        assertNotNull(repoKeyElement, "Couldn't find a repository key element.");

        String repoKey = repoKeyElement.getText();
        assertTrue(StringUtils.isNotBlank(repoKey), "Couldn't find a repository key value.");
        return repoKey;
    }

    private void assertNoType(String repoKey, Element repositoryElement) {
        assertNull(repositoryElement.getChild("type"),
                "Repository type definition should have been removed from: " + repoKey);
    }

    private void checkForDefaultLayouts(Element repoLayoutsElement, Namespace namespace) {
        List<Element> repoLayoutElements = repoLayoutsElement.getChildren();

        assertNotNull(repoLayoutElements, "Converted configuration should contain default repo layouts.");
        assertFalse(repoLayoutElements.isEmpty(),
                "Converted configuration should contain default repo layouts.");

        checkForDefaultM2Layout(repoLayoutElements, namespace);
        checkForDefaultIvyLayout(repoLayoutElements, namespace);
        checkForDefaultGradleLayout(repoLayoutElements, namespace);
        checkForDefaultM1Layout(repoLayoutElements, namespace);
    }

    private void checkForDefaultM2Layout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, RepoLayoutUtils.MAVEN_2_DEFAULT.getName(),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.MAVEN_2_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    private void checkForDefaultIvyLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, RepoLayoutUtils.IVY_DEFAULT.getName(),
                RepoLayoutUtils.IVY_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.IVY_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.IVY_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.IVY_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.IVY_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    private void checkForDefaultGradleLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, RepoLayoutUtils.GRADLE_DEFAULT.getName(),
                RepoLayoutUtils.GRADLE_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.GRADLE_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.GRADLE_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.GRADLE_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.GRADLE_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    private void checkForDefaultM1Layout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, RepoLayoutUtils.MAVEN_1_DEFAULT.getName(),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.MAVEN_1_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getFolderIntegrationRevisionRegExp());
    }

    private void checkLayout(List<Element> repoLayoutElements, Namespace namespace, String layoutName,
            String artifactPathPattern, String distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        boolean foundLayout = false;
        for (Element repoLayoutElement : repoLayoutElements) {
            if (layoutName.equals(repoLayoutElement.getChild("name", namespace).getText())) {
                checkLayoutElement(repoLayoutElement, namespace, layoutName, artifactPathPattern,
                        distinctiveDescriptorPathPattern, descriptorPathPattern, folderIntegrationRevisionRegExp,
                        fileIntegrationRevisionRegExp);
                foundLayout = true;
            }
        }
        assertTrue(foundLayout, "Could not find the default layout: " + layoutName);
    }

    private void checkLayoutElement(Element repoLayoutElement, Namespace namespace, String layoutName,
            String artifactPathPattern, String distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        checkLayoutField(repoLayoutElement, namespace, layoutName, "artifactPathPattern", artifactPathPattern,
                "artifact path pattern");

        checkLayoutField(repoLayoutElement, namespace, layoutName, "distinctiveDescriptorPathPattern",
                distinctiveDescriptorPathPattern, "distinctive descriptor path pattern");

        checkLayoutField(repoLayoutElement, namespace, layoutName, "descriptorPathPattern", descriptorPathPattern,
                "descriptor path pattern");

        checkLayoutField(repoLayoutElement, namespace, layoutName, "folderIntegrationRevisionRegExp",
                folderIntegrationRevisionRegExp, "folder integration revision reg exp");

        checkLayoutField(repoLayoutElement, namespace, layoutName, "fileIntegrationRevisionRegExp",
                fileIntegrationRevisionRegExp, "file integration revision reg exp");
    }

    private void checkLayoutField(Element repoLayoutElement, Namespace namespace, String layoutName, String childName,
            String expectedChildValue, String childDisplayName) {
        Element childElement = repoLayoutElement.getChild(childName, namespace);
        assertNotNull(childElement, "Could not find " + childDisplayName + " element in default repo layout: " +
                layoutName);
        assertEquals(childElement.getText(), expectedChildValue, "Unexpected " + childDisplayName +
                " in default repo layout: " + layoutName);
    }
}
