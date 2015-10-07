package org.artifactory.version.converter.v160;

import org.apache.commons.lang.StringUtils;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Shay Yaakov
 */
@Test
public class AddonsDefaultLayoutConverterTest extends XmlConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-no_addons_layouts.xml", new AddonsDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);
    }

    public void convertExisting() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-existing_addon_layout.xml", new AddonsDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);

        checkLayout(repoLayoutsElement.getChildren(), namespace, "art-nuget-default",
                "[orgPath]/[module]/[module].[baseRev](-[fileItegRev]).nupkg",
                "false",
                null,
                ".*",
                ".*");
    }

    public void convertExistingRandom() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-random_addon_layout.xml", new AddonsDefaultLayoutConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element repoLayoutsElement = rootElement.getChild("repoLayouts", namespace);
        assertNotNull(repoLayoutsElement, "Converted configuration should contain default repo layouts.");
        checkForDefaultLayouts(repoLayoutsElement, namespace);

        long nugetLayoutsCount = repoLayoutsElement.getChildren()
                .stream()
                .filter(element -> element.getChild("name", namespace).getText().contains("nuget"))
                .count();
        assertEquals(nugetLayoutsCount, 3);
    }

    private void checkForDefaultLayouts(Element repoLayoutsElement, Namespace namespace) {
        List<Element> repoLayoutElements = repoLayoutsElement.getChildren();

        assertNotNull(repoLayoutElements, "Converted configuration should contain default repo layouts.");
        assertFalse(repoLayoutElements.isEmpty(),
                "Converted configuration should contain default repo layouts.");

        checkForDefaultNuGetLayout(repoLayoutElements, namespace);
        checkForDefaultNpmLayout(repoLayoutElements, namespace);
        checkForDefaultBowerLayout(repoLayoutElements, namespace);
        checkForDefaultVcsLayout(repoLayoutElements, namespace);
        checkForDefaultSbtLayout(repoLayoutElements, namespace);
        checkForDefaultSimpleLayout(repoLayoutElements, namespace);
    }

    private void checkForDefaultNuGetLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "nuget-default",
                "[orgPath]/[module]/[module].[baseRev](-[fileItegRev]).nupkg",
                "false",
                null,
                ".*",
                ".*");
    }

    private void checkForDefaultNpmLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "npm-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).tgz",
                "false",
                null,
                ".*",
                ".*");
    }

    private void checkForDefaultBowerLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "bower-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false",
                null,
                ".*",
                ".*");
    }

    private void checkForDefaultVcsLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "vcs-default",
                "[orgPath]/[module]/[refs<tags|branches>]/[baseRev]/[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                "false",
                null,
                ".*",
                "[a-zA-Z0-9]{40}");
    }

    private void checkForDefaultSbtLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "sbt-default",
                "[org]/[module]/(scala_[scalaVersion<.+>])/(sbt_[sbtVersion<.+>])/[baseRev]/[type]s/[module](-[classifier]).[ext]",
                "true",
                "[org]/[module]/(scala_[scalaVersion<.+>])/(sbt_[sbtVersion<.+>])/[baseRev]/[type]s/ivy.xml",
                "\\d{14}",
                "\\d{14}");
    }

    private void checkForDefaultSimpleLayout(List<Element> repoLayoutElements, Namespace namespace) {
        checkLayout(repoLayoutElements, namespace, "simple-default",
                "[orgPath]/[module]/[module]-[baseRev](-[fileItegRev]).[ext]",
                "false",
                null,
                ".*",
                ".*");
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

        if (StringUtils.isNotBlank(descriptorPathPattern)) {
            checkLayoutField(repoLayoutElement, namespace, layoutName, "descriptorPathPattern", descriptorPathPattern,
                    "descriptor path pattern");
        } else {
            assertNull(repoLayoutElement.getChild("descriptorPathPattern"));
        }

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