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
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class DefaultRepoLayoutConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(DefaultRepoLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting the default repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Adding default global layouts");
        Element repoLayoutsElement = new Element("repoLayouts", namespace);
        repoLayoutsElement.addContent(getMaven2DefaultLayout(namespace));
        repoLayoutsElement.addContent(getIvyDefaultLayout(namespace));
        repoLayoutsElement.addContent(getGradleDefaultLayout(namespace));
        repoLayoutsElement.addContent(getMaven1DefaultLayout(namespace));
        rootElement.addContent(repoLayoutsElement);

        log.debug("Converting local repositories");
        Element localRepositoriesElement = rootElement.getChild("localRepositories", namespace);
        if (localRepositoriesElement != null) {
            List<Element> localRepositoryElements = localRepositoriesElement.getChildren("localRepository", namespace);
            if (localRepositoryElements != null && !localRepositoryElements.isEmpty()) {

                for (Element localRepositoryElement : localRepositoryElements) {
                    appendRepoLayoutRef(localRepositoryElement, namespace);
                    removeRepoType(localRepositoryElement, false, namespace);
                }
            }
        }

        log.debug("Converting remote repositories");
        Element remoteRepositoriesElement = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepositoriesElement != null) {
            List<Element> remoteRepositoryElements =
                    remoteRepositoriesElement.getChildren("remoteRepository", namespace);
            if (remoteRepositoryElements != null && !remoteRepositoryElements.isEmpty()) {

                for (Element remoteRepositoryElement : remoteRepositoryElements) {
                    appendRepoLayoutRef(remoteRepositoryElement, namespace);
                    removeRepoType(remoteRepositoryElement, true, namespace);
                }
            }
        }

        log.debug("Converting virtual repositories");
        Element virtualRepositoriesElement = rootElement.getChild("virtualRepositories", namespace);
        if (virtualRepositoriesElement != null) {
            List<Element> virtualRepositoryElements =
                    virtualRepositoriesElement.getChildren("virtualRepository", namespace);
            if (virtualRepositoryElements != null && !virtualRepositoryElements.isEmpty()) {

                for (Element virtualRepositoryElement : virtualRepositoryElements) {
                    removeRepoType(virtualRepositoryElement, false, namespace);
                }
            }
        }

        log.info("Ending the default repository layout conversion.");
    }

    private Element getMaven2DefaultLayout(Namespace namespace) {
        return getRepoLayoutElement(namespace,
                RepoLayoutUtils.MAVEN_2_DEFAULT.getName(),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.MAVEN_2_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.MAVEN_2_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    private Element getIvyDefaultLayout(Namespace namespace) {
        return getRepoLayoutElement(namespace,
                RepoLayoutUtils.IVY_DEFAULT.getName(),
                RepoLayoutUtils.IVY_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.IVY_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.IVY_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.IVY_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.IVY_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    private Element getGradleDefaultLayout(Namespace namespace) {
        return getRepoLayoutElement(namespace,
                RepoLayoutUtils.GRADLE_DEFAULT.getName(),
                RepoLayoutUtils.GRADLE_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.GRADLE_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.GRADLE_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.GRADLE_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.GRADLE_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    private Element getMaven1DefaultLayout(Namespace namespace) {
        return getRepoLayoutElement(namespace,
                RepoLayoutUtils.MAVEN_1_DEFAULT.getName(),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getArtifactPathPattern(),
                Boolean.toString(RepoLayoutUtils.MAVEN_1_DEFAULT.isDistinctiveDescriptorPathPattern()),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getDescriptorPathPattern(),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getFolderIntegrationRevisionRegExp(),
                RepoLayoutUtils.MAVEN_1_DEFAULT.getFileIntegrationRevisionRegExp());
    }

    public Element getRepoLayoutElement(Namespace namespace, String name, String artifactPathPattern,
            String distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        Element repoLayoutElement = new Element("repoLayout", namespace);

        Element nameElement = new Element("name", namespace);
        nameElement.setText(name);
        repoLayoutElement.addContent(nameElement);

        Element artifactPathPatternElement = new Element("artifactPathPattern", namespace);
        artifactPathPatternElement.setText(artifactPathPattern);
        repoLayoutElement.addContent(artifactPathPatternElement);

        Element distinctiveDescriptorPathPatternElement = new Element("distinctiveDescriptorPathPattern", namespace);
        distinctiveDescriptorPathPatternElement.setText(distinctiveDescriptorPathPattern);
        repoLayoutElement.addContent(distinctiveDescriptorPathPatternElement);

        Element descriptorPathPatternElement = new Element("descriptorPathPattern", namespace);
        descriptorPathPatternElement.setText(descriptorPathPattern);
        repoLayoutElement.addContent(descriptorPathPatternElement);

        if (StringUtils.isNotBlank(folderIntegrationRevisionRegExp)) {
            Element folderIntegrationRevisionRegExpElement = new Element("folderIntegrationRevisionRegExp", namespace);
            folderIntegrationRevisionRegExpElement.setText(folderIntegrationRevisionRegExp);
            repoLayoutElement.addContent(folderIntegrationRevisionRegExpElement);
        }

        if (StringUtils.isNotBlank(fileIntegrationRevisionRegExp)) {
            Element fileIntegrationRevisionRegExpElement = new Element("fileIntegrationRevisionRegExp", namespace);
            fileIntegrationRevisionRegExpElement.setText(fileIntegrationRevisionRegExp);
            repoLayoutElement.addContent(fileIntegrationRevisionRegExpElement);
        }

        return repoLayoutElement;
    }

    private void appendRepoLayoutRef(Element repositoryElement, Namespace namespace) {
        Element repoLayoutRefElement = new Element("repoLayoutRef", namespace);
        repoLayoutRefElement.setText(RepoLayoutUtils.MAVEN_2_DEFAULT_NAME);

        log.debug("Appending default layout reference to '{}'", repositoryElement.getChild("key", namespace).getText());
        appendElementAfter(repositoryElement, repoLayoutRefElement, namespace, "excludesPattern", "includesPattern",
                "notes", "type", "description", "key");
    }

    private void removeRepoType(Element repositoryElement, boolean isRemote, Namespace namespace) {
        Element typeElement = repositoryElement.getChild("type", namespace);
        if (typeElement != null) {

            String repoKey = repositoryElement.getChild("key", namespace).getText();
            log.debug("Removing repository type definition from '{}'", repoKey);
            repositoryElement.removeChild("type", namespace);

            if (isRemote && "maven1".equals(typeElement.getText())) {

                Element remoteRepoLayoutRefElement = new Element("remoteRepoLayoutRef", namespace);
                remoteRepoLayoutRefElement.setText(RepoLayoutUtils.MAVEN_1_DEFAULT_NAME);

                log.debug("Appending Maven 1 remote repository layout reference to '{}'", repoKey);
                appendElementAfter(repositoryElement, remoteRepoLayoutRefElement, namespace,
                        "listRemoteFolderItems", "synchronizeProperties", "shareConfiguration",
                        "unusedArtifactsCleanupPeriodHours", "unusedArtifactsCleanupEnabled",
                        "remoteRepoChecksumPolicyType", "missedRetrievalCachePeriodSecs",
                        "failedRetrievalCachePeriodSecs", "retrievalCachePeriodSecs", "fetchSourcesEagerly",
                        "fetchJarsEagerly", "storeArtifactsLocally", "hardFail", "offline", "url");
            }
        }
    }

    private void appendElementAfter(Element appendTo, Element toAppend, Namespace namespace,
            String... elementNamesToAppendAfter) {
        int indexToAppendAfter = getIndexOfFirstFoundElement(appendTo, namespace, elementNamesToAppendAfter);

        appendTo.addContent(indexToAppendAfter + 1, toAppend);
    }

    private int getIndexOfFirstFoundElement(Element repositoryElement, Namespace namespace, String... elementNames) {
        for (String elementName : elementNames) {
            Element child = repositoryElement.getChild(elementName, namespace);
            if (child != null) {
                return repositoryElement.indexOf(child);
            }
        }

        return -1;
    }
}
