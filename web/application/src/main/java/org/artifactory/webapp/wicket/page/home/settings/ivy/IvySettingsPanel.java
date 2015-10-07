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

package org.artifactory.webapp.wicket.page.home.settings.ivy;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.webapp.wicket.page.home.settings.ivy.base.BaseIvySettingsGeneratorPanel;
import org.artifactory.webapp.wicket.page.home.settings.ivy.base.IvySettingsRepoSelectorPanel;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Enables the user to select a virtual repo that are configured in the system and if needed, to modify resolver name,
 * artifact patterns and Ivy patterns to generate an ivysettings.xml
 *
 * @author Noam Y. Tenne
 */
public class IvySettingsPanel extends BaseIvySettingsGeneratorPanel {

    private static final Logger log = LoggerFactory.getLogger(IvySettingsPanel.class);
    private IvySettingsRepoSelectorPanel resolverPanel;

    /**
     * Main constructor
     *
     * @param id                     ID to assign to the panel
     * @param servletContextUrl      Running context URL
     * @param virtualRepoDescriptors List of virtual repository descriptors
     */
    public IvySettingsPanel(String id, String servletContextUrl,
            List<? extends RepoDescriptor> virtualRepoDescriptors) {
        super(id, servletContextUrl);

        //noinspection unchecked
        resolverPanel = new IvySettingsRepoSelectorPanel("resolverPanel", virtualRepoDescriptors, servletContextUrl,
                IvySettingsRepoSelectorPanel.RepoType.LIBS);
        form.add(resolverPanel);
    }

    @Override
    public String generateSettings() {
        Document document = new Document();
        Element rootNode = new Element("ivy-settings");

        Element settingsElement = new Element("settings");
        settingsElement.setAttribute("defaultResolver", "main");
        rootNode.addContent(settingsElement);

        if (!authorizationService.isAnonymous() || !authorizationService.isAnonAccessEnabled()) {
            rootNode.addContent(
                    new Comment("Authentication required for publishing (deployment). 'Artifactory Realm' is " +
                            "the realm used by Artifactory so don't change it."));

            Element credentialsElement = new Element("credentials");
            try {
                credentialsElement.setAttribute("host", new URL(servletContextUrl).getHost());
            } catch (MalformedURLException e) {
                String errorMessage =
                        "An error occurred while decoding the servlet context URL for the credentials host attribute: ";
                error(errorMessage + e.getMessage());
                log.error(errorMessage, e);
            }
            credentialsElement.setAttribute("realm", "Artifactory Realm");

            FilteredResourcesWebAddon filteredResourcesWebAddon =
                    addonsManager.addonByType(FilteredResourcesWebAddon.class);

            credentialsElement.setAttribute("username",
                    filteredResourcesWebAddon.getGeneratedSettingsUsernameTemplate());

            credentialsElement.setAttribute("passwd", "@PASS_ATTR_PLACEHOLDER@");

            rootNode.addContent(credentialsElement);
        }

        Element resolversElement = new Element("resolvers");

        Element chainElement = new Element("chain");
        chainElement.setAttribute("name", "main");

        String resolverName = resolverPanel.getResolverName();
        resolverName = StringUtils.isNotBlank(resolverName) ? resolverName : "public";

        if (resolverPanel.useIbiblioResolver()) {

            Element ibiblioElement = new Element("ibiblio");
            ibiblioElement.setAttribute("name", resolverName);
            ibiblioElement.setAttribute("m2compatible", Boolean.TRUE.toString());
            ibiblioElement.setAttribute("root", resolverPanel.getFullRepositoryUrl());
            chainElement.addContent(ibiblioElement);
        } else {

            Element urlElement = new Element("url");
            urlElement.setAttribute("name", resolverName);

            urlElement.setAttribute("m2compatible", Boolean.toString(resolverPanel.isM2Compatible()));

            Element artifactPatternElement = new Element("artifact");
            artifactPatternElement.setAttribute("pattern", resolverPanel.getFullArtifactPattern());
            urlElement.addContent(artifactPatternElement);

            Element ivyPatternElement = new Element("ivy");
            ivyPatternElement.setAttribute("pattern", resolverPanel.getFullDescriptorPattern());
            urlElement.addContent(ivyPatternElement);

            chainElement.addContent(urlElement);
        }

        resolversElement.addContent(chainElement);

        rootNode.addContent(resolversElement);

        document.setRootElement(rootNode);

        String result = new XMLOutputter(Format.getPrettyFormat()).outputString(document);
        // after the xml is generated replace the password placeholder with the template placeholder (otherwise jdom
        // escapes this string)
        FilteredResourcesWebAddon filteredResourcesWebAddon =
                addonsManager.addonByType(FilteredResourcesWebAddon.class);
        return result.replace("@PASS_ATTR_PLACEHOLDER@",
                filteredResourcesWebAddon.getGeneratedSettingsUserCredentialsTemplate(false));
    }

    @Override
    protected String getGenerateButtonTitle() {
        return "Generate Settings";
    }

    @Override
    protected String getSettingsWindowTitle() {
        return "Ivy Settings";
    }

    @Override
    protected Syntax getSettingsSyntax() {
        return Syntax.xml;
    }

    @Override
    protected String getSettingsMimeType() {
        return "application/xml";
    }

    @Override
    protected String getSaveToFileName() {
        return "ivysettings.xml";
    }

    @Override
    protected String getDownloadButtonTitle() {
        return "Download Settings";
    }
}