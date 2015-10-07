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

package org.artifactory.webapp.wicket.page.home.settings.ivy.gradle;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.resource.StringResourceStream;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.webapp.wicket.page.home.settings.BaseSettingsGeneratorPanel;
import org.artifactory.webapp.wicket.page.home.settings.modal.download.AjaxSettingsDownloadBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

/**
 * Enables the user to select a virtual repo that are configured in the system and if needed, to modify resolver name,
 * artifact patterns and ivy patterns to generate an artifactory gradle init script
 *
 * @author Noam Y. Tenne
 */
public class GradleBuildScriptPanel extends BaseSettingsGeneratorPanel {

    private static final Logger log = LoggerFactory.getLogger(GradleBuildScriptPanel.class);
    private GradleBuildScriptRepoSelectorPanel pluginsResolverPanel;
    private GradleBuildScriptRepoSelectorPanel libsResolverPanel;
    private GradleBuildScriptRepoSelectorPanel libsPublisherPanel;
    private final String servletContextUrl;
    private final String JCENTER_DUMMY_REPO_KEY = "Bintray -> jcenter";

    /**
     * Main constructor
     *
     * @param id                     ID to assign to the panel
     * @param servletContextUrl      Running context URL
     * @param virtualRepoDescriptors List of virtual repository descriptors
     */
    protected GradleBuildScriptPanel(String id, final String servletContextUrl,
            List<? extends RepoDescriptor> virtualRepoDescriptors,
            List<? extends RepoDescriptor> localRepoDescriptors) {
        super(id, servletContextUrl);
        this.servletContextUrl = servletContextUrl;

        TitledBorder border = new TitledBorder("settingsBorder");
        TitledAjaxSubmitLink generateButton = getGenerateButton();
        form.add(new DefaultButtonBehavior(generateButton));

        final AjaxSettingsDownloadBehavior ajaxSettingsDownloadBehavior =
                new AjaxSettingsDownloadBehavior(getSavePropsToFileName()) {

                    @Override
                    protected StringResourceStream getResourceStream() {
                        FilteredResourcesWebAddon filteredResourcesWebAddon = addonsManager.addonByType(
                                FilteredResourcesWebAddon.class);

                        java.util.Properties credentialProperties = new java.util.Properties();
                        credentialProperties.setProperty("auth.username",
                                filteredResourcesWebAddon.getGeneratedSettingsUsernameTemplate());

                        credentialProperties.setProperty("auth.password",
                                filteredResourcesWebAddon.getGeneratedSettingsUserCredentialsTemplate(false));

                        credentialProperties.setProperty("auth.contextUrl", servletContextUrl);

                        String content = replaceValuesAndGetTemplateValue("/gradle.properties.template",
                                credentialProperties);

                        try {
                            String filtered = filteredResourcesWebAddon.filterResource(null,
                                    (org.artifactory.md.Properties) InfoFactoryHolder.get().createProperties(),
                                    new StringReader(content));
                            return new StringResourceStream(filtered);
                        } catch (Exception e) {
                            log.error("Unable to filter gradle build properties file: " + e.getMessage());
                            return new StringResourceStream(content);
                        }
                    }
                };

        String downloadPropsButtonTitle = getDownloadPropsButtonTitle();
        TitledAjaxSubmitLink downloadPropsLink = new TitledAjaxSubmitLink("downloadProps", downloadPropsButtonTitle,
                form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ajaxSettingsDownloadBehavior.initiate(target);
            }
        };
        form.add(new DefaultButtonBehavior(downloadPropsLink));

        //Add a dummy repo descriptor for jcenter
        List<RepoDescriptor> pluginResolverRepos = Lists.newArrayList();
        LocalRepoDescriptor jcenter = new LocalRepoDescriptor();
        jcenter.setKey(JCENTER_DUMMY_REPO_KEY);
        pluginResolverRepos.add(jcenter);
        pluginResolverRepos.addAll(virtualRepoDescriptors);

        pluginsResolverPanel = new GradleBuildScriptRepoSelectorPanel("pluginsResolverPanel", pluginResolverRepos,
                servletContextUrl, GradleBuildScriptRepoSelectorPanel.RepoType.PLUGINS_RESOLVER, true);
        form.add(pluginsResolverPanel);

        libsResolverPanel = new GradleBuildScriptRepoSelectorPanel("libsResolverPanel", virtualRepoDescriptors,
                servletContextUrl, GradleBuildScriptRepoSelectorPanel.RepoType.LIBS_RESOLVER, true);
        form.add(libsResolverPanel);

        libsPublisherPanel = new GradleBuildScriptRepoSelectorPanel("libsPublisherPanel", localRepoDescriptors,
                servletContextUrl, GradleBuildScriptRepoSelectorPanel.RepoType.LIBS_PUBLISHER, false);
        form.add(libsPublisherPanel);

        border.add(form);
        add(border);
        add(ajaxSettingsDownloadBehavior);
        add(generateButton);
        add(downloadPropsLink);
    }

    @Override
    public String generateSettings() {
        Properties templateProperties = getTemplateProperties();

        return replaceValuesAndGetTemplateValue("/build.gradle.template", templateProperties);
    }

    @Override
    protected String getGenerateButtonTitle() {
        return "Generate Build Script";
    }

    @Override
    protected String getSettingsWindowTitle() {
        return "Gradle Build Script";
    }

    @Override
    protected Syntax getSettingsSyntax() {
        return Syntax.groovy;
    }

    @Override
    protected String getSettingsMimeType() {
        return "application/x-groovy";
    }

    @Override
    protected String getSaveToFileName() {
        return "build.gradle";
    }

    @Override
    protected String getDownloadButtonTitle() {
        return "Download Script";
    }

    private Properties getTemplateProperties() {
        Properties repoDetailsProperties = new Properties();

        // General
        repoDetailsProperties.setProperty("artifactory.contextUrl", servletContextUrl);
        repoDetailsProperties.setProperty("gradle.build.1tab", "    ");
        repoDetailsProperties.setProperty("gradle.build.2tabs", "        ");
        repoDetailsProperties.setProperty("gradle.build.3tabs", "            ");
        repoDetailsProperties.setProperty("gradle.build.4tabs", "                ");

        //jcenter overrides both maven and ivy
        if(pluginsResolverPanel.getSelectedRepositoryKey().equals(JCENTER_DUMMY_REPO_KEY)) {
            repoDetailsProperties.setProperty("maven.repo", "jcenter()");
            repoDetailsProperties.setProperty("ivy.repo", "");
        // Plugins
        } else {
            repoDetailsProperties.setProperty("plugins.repository.url", pluginsResolverPanel.getFullRepositoryUrl());
            if (pluginsResolverPanel.isUseMaven()) {
                repoDetailsProperties.setProperty("maven.repo",
                        replaceValuesAndGetTemplateValue("/build.gradle.maven.repo.template", repoDetailsProperties));
            } else {
                repoDetailsProperties.setProperty("maven.repo", "");
            }

            if (pluginsResolverPanel.isUseIvy()) {
                repoDetailsProperties.setProperty("ivy.repo",
                        replaceValuesAndGetTemplateValue("/build.gradle.ivy.repo.template", repoDetailsProperties));
            } else {
                repoDetailsProperties.setProperty("ivy.repo", "");
            }
        }
        // Publisher
        if (libsPublisherPanel.isUseIvy()) {
            repoDetailsProperties.setProperty("ivy.publisher",
                    replaceValuesAndGetTemplateValue("/build.gradle.ivy.publisher.template", repoDetailsProperties));
            repoDetailsProperties.setProperty("libs.publisher.ivy.pattern",
                    libsPublisherPanel.getDescriptorPattern());
            repoDetailsProperties.setProperty("libs.publisher.artifact.pattern",
                    libsPublisherPanel.getArtifactPattern());
            repoDetailsProperties.setProperty("libs.publisher.maven.compatible",
                    Boolean.toString(libsPublisherPanel.getFullDescriptorPattern().contains("pom")));
        } else {
            repoDetailsProperties.setProperty("ivy.publisher", "");
        }
        repoDetailsProperties.setProperty("libs.publisher.repoKey", libsPublisherPanel.getSelectedRepositoryKey());
        repoDetailsProperties.setProperty("libs.publisher.maven", Boolean.toString(libsPublisherPanel.isUseMaven()));

        // Resolver
        if (libsResolverPanel.isUseIvy()) {
            repoDetailsProperties.setProperty("ivy.resolver",
                    replaceValuesAndGetTemplateValue("/build.gradle.ivy.resolver.template", repoDetailsProperties));
            repoDetailsProperties.setProperty("libs.resolver.ivy.pattern",
                    libsResolverPanel.getDescriptorPattern());
            repoDetailsProperties.setProperty("libs.resolver.artifact.pattern",
                    libsResolverPanel.getArtifactPattern());
            repoDetailsProperties.setProperty("libs.resolver.maven.compatible",
                    Boolean.toString(libsResolverPanel.getFullDescriptorPattern().contains("pom")));
        } else {
            repoDetailsProperties.setProperty("ivy.resolver", "");
        }
        repoDetailsProperties.setProperty("libs.resolver.repoKey", libsResolverPanel.getSelectedRepositoryKey());
        repoDetailsProperties.setProperty("libs.resolver.maven", Boolean.toString(libsResolverPanel.isUseMaven()));

        // Credentials
        Properties credentialProperties = new Properties();
        String resolveCreds;
        String publishCreds;
        String pluginsCreds;
        if (!authorizationService.isAnonymous() || !authorizationService.isAnonAccessEnabled()) {
            credentialProperties.setProperty("creds.line.break", "\n            ");
            resolveCreds = replaceValuesAndGetTemplateValue("/build.gradle.resolve.creds.template",
                    credentialProperties);
            publishCreds = replaceValuesAndGetTemplateValue("/build.gradle.publish.creds.template",
                    credentialProperties);
            pluginsCreds = replaceValuesAndGetTemplateValue("/build.gradle.creds.template", credentialProperties);
        } else {
            resolveCreds = "";
            publishCreds = "";
            pluginsCreds = "";
        }
        repoDetailsProperties.setProperty("resolve.creds", resolveCreds);
        repoDetailsProperties.setProperty("publish.creds", publishCreds);
        repoDetailsProperties.setProperty("repo.creds", pluginsCreds);

        return repoDetailsProperties;
    }

    private String replaceValuesAndGetTemplateValue(String templateResourcePath, Properties templateProperties) {
        InputStream gradleInitTemplateStream = null;
        try {
            gradleInitTemplateStream = getClass().getResourceAsStream(templateResourcePath);
            String gradleTemplate = IOUtils.toString(gradleInitTemplateStream);
            PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
            return propertyPlaceholderHelper.replacePlaceholders(gradleTemplate, templateProperties);
        } catch (IOException e) {
            String errorMessage = "An error occurred while preparing the Gradle Init Script template: ";
            error(errorMessage + e.getMessage());
            log.error(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(gradleInitTemplateStream);
        }
        return "";
    }

    private String getDownloadPropsButtonTitle() {
        return "Download gradle.properties";
    }

    private String getSavePropsToFileName() {
        return "gradle.properties";
    }
}