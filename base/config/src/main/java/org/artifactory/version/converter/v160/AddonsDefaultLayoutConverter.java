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

package org.artifactory.version.converter.v160;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shay Yaakov
 */
public class AddonsDefaultLayoutConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(AddonsDefaultLayoutConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting the default addons repository layout conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Adding default addons global layouts");
        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        addNuGetDefaultLayout(repoLayoutsElement, namespace);
        addNpmDefaultLayout(repoLayoutsElement, namespace);
        addBowerDefaultLayout(repoLayoutsElement, namespace);
        addVcsDefaultLayout(repoLayoutsElement, namespace);
        addSbtDefaultLayout(repoLayoutsElement, namespace);
        addSimpleDefaultLayout(repoLayoutsElement, namespace);

        log.info("Ending the default addons repository layout conversion");
    }

    private void addNuGetDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(getRepoLayoutElement(repoLayoutsElement, namespace,
                "nuget-default",
                "[orgPath]/[module]/[module].[baseRev](-[fileItegRev]).nupkg",
                "false", null,
                ".*",
                ".*"));
    }

    private void addNpmDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(getRepoLayoutElement(repoLayoutsElement, namespace,
                "npm-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).tgz",
                "false", null,
                ".*",
                ".*"));
    }

    private void addBowerDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(getRepoLayoutElement(repoLayoutsElement, namespace,
                "bower-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false", null,
                ".*",
                ".*"));
    }

    private void addVcsDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(getRepoLayoutElement(repoLayoutsElement, namespace,
                "vcs-default",
                "[orgPath]/[module]/[refs<tags|branches>]/[baseRev]/[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                "false", null,
                ".*",
                "[a-zA-Z0-9]{40}"));
    }

    private void addSbtDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(getRepoLayoutElement(repoLayoutsElement, namespace,
                "sbt-default",
                "[org]/[module]/(scala_[scalaVersion<.+>])/(sbt_[sbtVersion<.+>])/[baseRev]/[type]s/[module](-[classifier]).[ext]",
                "true",
                "[org]/[module]/(scala_[scalaVersion<.+>])/(sbt_[sbtVersion<.+>])/[baseRev]/[type]s/ivy.xml",
                "\\d{14}",
                "\\d{14}"));
    }

    private void addSimpleDefaultLayout(Element repoLayoutsElement, Namespace namespace) {
        repoLayoutsElement.addContent(getRepoLayoutElement(repoLayoutsElement, namespace,
                "simple-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false", null,
                ".*",
                ".*"));
    }

    public Element getRepoLayoutElement(Element repoLayoutsElement, Namespace namespace, String name, String artifactPathPattern,
                                        String distinctiveDescriptorPathPattern, String descriptorPathPattern,
                                        String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {

        // Maybe the user already configured *-default, if so randomize the name
        for (Element repoLayoutElement : repoLayoutsElement.getChildren()) {
            if (name.equals(repoLayoutElement.getChild("name", namespace).getText())) {
                if (repoLayoutsElement.getChild("art-" + name, namespace) != null) {
                    name += RandomStringUtils.randomNumeric(3);
                } else {
                    name = "art-" + name;
                }
            }
        }

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

        if (StringUtils.isNotBlank(descriptorPathPattern)) {
            Element descriptorPathPatternElement = new Element("descriptorPathPattern", namespace);
            descriptorPathPatternElement.setText(descriptorPathPattern);
            repoLayoutElement.addContent(descriptorPathPatternElement);
        }

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
}
