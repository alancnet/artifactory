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

package org.artifactory.webapp.wicket.page.home.settings.maven;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.api.maven.MavenSettings;
import org.artifactory.api.maven.MavenSettingsMirror;
import org.artifactory.api.maven.MavenSettingsRepository;
import org.artifactory.api.maven.MavenSettingsServer;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.webapp.wicket.page.home.settings.BaseSettingsGeneratorPanel;
import org.artifactory.webapp.wicket.page.home.settings.DefaultOptionSelector;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Enables the user to select several virtual repos that are configured in the system, and generate from his selection A
 * "repositories", and "mirrors" block that can be used with the maven settings.xml
 *
 * @author Noam Tenne
 */
public class MavenSettingsPanel<T extends RepoDescriptor> extends BaseSettingsGeneratorPanel {

    private static final Logger log = LoggerFactory.getLogger(MavenSettingsPanel.class);

    @WicketProperty
    private T releases;

    @WicketProperty
    private T snapshots;

    @WicketProperty
    private T pluginReleases;

    @WicketProperty
    private T pluginSnapshots;

    @WicketProperty
    private boolean mirrorAny;

    @WicketProperty
    private T mirrorAnySelection;
    private final List<T> virtualRepoDescriptors;

    /**
     * Default Constructor
     *
     * @param id                     Panel ID
     * @param servletContextUrl      Servlet context URL
     * @param virtualRepoDescriptors List of virtual repository descriptors
     */
    public MavenSettingsPanel(String id, String servletContextUrl, List<T> virtualRepoDescriptors) {
        super(id, servletContextUrl);
        this.virtualRepoDescriptors = virtualRepoDescriptors;

        init();
    }

    /**
     * Init panel components
     */
    private void init() {
        TitledBorder border = new TitledBorder("mavenSettingsBorder");

        addChoice("releases", true, false, false, false);
        addChoice("snapshots", false, true, false, false);
        addChoice("pluginReleases", true, false, true, false);
        addChoice("pluginSnapshots", false, true, true, false);

        final DropDownChoice mirrorDropDownChoice = new DropDownChoice<>("mirrorAnySelection",
                new PropertyModel<T>(this, "mirrorAnySelection"), virtualRepoDescriptors);
        mirrorDropDownChoice.setOutputMarkupId(true);
        if (!virtualRepoDescriptors.isEmpty()) {
            mirrorDropDownChoice.setDefaultModelObject(getDefaultChoice(false, false, false, true));
        }
        mirrorDropDownChoice.setEnabled(false);
        form.add(mirrorDropDownChoice);
        form.add(new HelpBubble("mirrorAnySelection.help", new ResourceModel("mirrorAnySelection.help")));

        final StyledCheckbox mirrorAnyCheckbox =
                new StyledCheckbox("mirrorAny", new PropertyModel<Boolean>(this, "mirrorAny"));
        mirrorAnyCheckbox.setDefaultModelObject(Boolean.FALSE);
        mirrorAnyCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                mirrorDropDownChoice.setEnabled(mirrorAnyCheckbox.isChecked());
                target.add(mirrorDropDownChoice);
            }
        });
        form.add(mirrorAnyCheckbox);

        TitledAjaxSubmitLink generateButton = getGenerateButton();

        form.add(new DefaultButtonBehavior(generateButton));
        border.add(form);
        add(border);
        add(generateButton);
    }

    /**
     * Returns is mirror any selected
     *
     * @return boolean - Is mirror any selected
     */
    public boolean isMirrorAny() {
        return mirrorAny;
    }

    public void setMirrorAny(boolean mirrorAny) {
        this.mirrorAny = mirrorAny;
    }

    @Override
    public String generateSettings() {
        //Make sure URL ends with slash
        if (!servletContextUrl.endsWith("/")) {
            servletContextUrl += "/";
        }

        MavenSettings mavenSettings = assembleLocalSettingsModel(servletContextUrl);

        try {
            //Generate XML settings content
            return mavenService.generateSettings(mavenSettings);
        } catch (
                IOException ioe) {
            String message = ioe.getMessage();
            if (message == null) {
                log.error("Maven settings generator error", ioe);
                String logs;
                if (authorizationService.isAdmin()) {
                    String systemLogsPage = WicketUtils.absoluteMountPathForPage(SystemLogsPage.class);
                    logs = "<a href=\"" + systemLogsPage + "\">log</a>";
                } else {
                    logs = "log";
                }
                message = "Please review the " + logs + " for further information.";
            }
            error(new UnescapedFeedbackMessage("An error has occurred during maven setting generation: " + message));
            return "Maven settings could no be generated.";
        }
    }

    @Override
    protected String getGenerateButtonTitle() {
        return "Generate Settings";
    }

    @Override
    protected String getSettingsWindowTitle() {
        return "Maven Settings";
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
        return "settings.xml";
    }

    @Override
    protected String getDownloadButtonTitle() {
        return "Download Settings";
    }

    /**
     * Prepares a local maven settings model to be used by the maven service.<br>
     * Published as a protected method because a simple extension point is needed by the dashboard to append servers to
     * the model.
     *
     * @param servletContextUrl Current context URL
     * @return Local settings model
     */
    protected MavenSettings assembleLocalSettingsModel(String servletContextUrl) {
        //Build settings object from the user selections in the panel
        MavenSettings mavenSettings = new MavenSettings(servletContextUrl);

        //Add release and snapshot choices
        String releases = getReleasesRepoKey();
        mavenSettings.addReleaseRepository(new MavenSettingsRepository("central", releases, false));
        String snapshots = getSnapshotsRepoKey();
        mavenSettings.addReleaseRepository(new MavenSettingsRepository("snapshots", snapshots, true));

        //Add plugin release and snapshot choices
        String pluginReleases = getPluginReleasesRepoKey();
        mavenSettings.addPluginRepository(new MavenSettingsRepository("central", pluginReleases, false));
        String pluginSnapshots = getPluginSnapshotsRepoKey();
        mavenSettings.addPluginRepository(new MavenSettingsRepository("snapshots", pluginSnapshots, true));

        //Add the "mirror any" repo, if the user has selected it
        if (isMirrorAny()) {
            String mirror = getMirrorAnyKey();
            mavenSettings.addMirrorRepository(new MavenSettingsMirror(mirror, mirror, "*"));
        }

        if (!authorizationService.isAnonymous() || !authorizationService.isAnonAccessEnabled()) {
            FilteredResourcesWebAddon filteredResourcesWebAddon =
                    addonsManager.addonByType(FilteredResourcesWebAddon.class);

            String currentUsername = filteredResourcesWebAddon.getGeneratedSettingsUsernameTemplate();
            String currentPassword = filteredResourcesWebAddon.getGeneratedSettingsUserCredentialsTemplate(true);

            mavenSettings.addServer(new MavenSettingsServer("central", currentUsername, currentPassword));
            mavenSettings.addServer(new MavenSettingsServer("snapshots", currentUsername, currentPassword));
        }
        return mavenSettings;
    }

    /**
     * Returns the selected release repo key
     *
     * @return String - Release repo key
     */
    private String getReleasesRepoKey() {
        return releases.getKey();
    }

    /**
     * Returns the selected snapshot repo key
     *
     * @return String - Snapshot repo key
     */
    private String getSnapshotsRepoKey() {
        return snapshots.getKey();
    }

    /**
     * Returns the selected plugin releases repo key
     *
     * @return String - Plugin releases repo key
     */
    private String getPluginReleasesRepoKey() {
        return pluginReleases.getKey();
    }

    /**
     * Returns the selected plugin snapshots repo key
     *
     * @return String - Plugin snapshots repo key
     */
    private String getPluginSnapshotsRepoKey() {
        return pluginSnapshots.getKey();
    }

    /**
     * Returns the selected mirror any repo key
     *
     * @return String - Mirror any repo key
     */
    private String getMirrorAnyKey() {
        return mirrorAnySelection.getKey();
    }

    /**
     * Adds to the form a DropDownChoice. Sets the object a property model with the id as the expression and adds the
     * virtualRepoEntries list to it.
     *
     * @param id Object ID
     */
    private void addChoice(String id, boolean isRelease, boolean isSnapshot, boolean isPlugin, boolean isRemote) {
        DropDownChoice<T> choice = new DropDownChoice<>(id, new PropertyModel<T>(this, id), virtualRepoDescriptors);
        if (!virtualRepoDescriptors.isEmpty()) {
            choice.setDefaultModelObject(getDefaultChoice(isRelease, isSnapshot, isPlugin, isRemote));
        }
        form.add(choice);
        form.add(new HelpBubble(id + ".help", new ResourceModel(id + ".help")));
    }


    private T getDefaultChoice(final boolean isRelease, final boolean isSnapshot, final boolean isPlugin,
            final boolean isRemote) {

        DefaultOptionSelector<RepoDescriptor> selector = new DefaultOptionSelector<RepoDescriptor>() {

            @Override
            public boolean acceptedAsDefault(RepoDescriptor virtualRepoDescriptor) {
                boolean canBeDefault = true;
                String key = virtualRepoDescriptor.getKey();
                if (isRelease && !repoKeyContains(key, "release")) {
                    canBeDefault = false;
                }
                if (isSnapshot && !repoKeyContains(key, "snapshot")) {
                    canBeDefault = false;
                }
                if (isPlugin && !repoKeyContains(key, "plugin")) {
                    canBeDefault = false;
                }
                if (isRemote && !repoKeyContains(key, "remote")) {
                    canBeDefault = false;
                }

                return canBeDefault;
            }

            private boolean repoKeyContains(String repoKey, String idText) {
                return StringUtils.containsIgnoreCase(repoKey, idText);
            }
        };

        for (T virtualRepoDescriptor : virtualRepoDescriptors) {
            if (selector.acceptedAsDefault(virtualRepoDescriptor)) {
                return virtualRepoDescriptor;
            }
        }

        return virtualRepoDescriptors.get(0);
    }

}