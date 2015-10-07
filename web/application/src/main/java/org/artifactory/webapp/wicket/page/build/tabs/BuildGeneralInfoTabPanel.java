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

package org.artifactory.webapp.wicket.page.build.tabs;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.wicket.SearchAddon;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.webapp.wicket.page.browse.treebrowser.action.BintrayBuildPanel;
import org.artifactory.webapp.wicket.page.search.SaveSearchResultsPanel;
import org.artifactory.webapp.wicket.page.security.profile.ProfilePage;
import org.jfrog.build.api.Agent;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildAgent;
import org.jfrog.build.api.IssueTracker;
import org.jfrog.build.api.Issues;

/**
 * Displays the build's general information
 *
 * @author Noam Y. Tenne
 */
public class BuildGeneralInfoTabPanel extends Panel {

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private BintrayService bintrayService;

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private CentralConfigService centralConfigService;
    @SpringBean
    private UserGroupService userGroupService;

    /**
     * Main constructor
     *
     * @param id    ID to assign to the panel
     * @param build Build to display
     */
    public BuildGeneralInfoTabPanel(String id, Build build) {
        super(id);

        addInfoBorder(build);
        addSaveSearchResultsPanel(build);
    }

    /**
     * Adds the general information border
     *
     * @param build Build object to display
     */
    private void addInfoBorder(Build build) {
        FieldSetBorder infoBorder = new FieldSetBorder("infoBorder");
        add(infoBorder);

        addLabeledValue(infoBorder, "name", "Name", build.getName(), true);
        addLabeledValue(infoBorder, "number", "Number", build.getNumber(), true);

        Agent agent = build.getAgent();
        String agentName = null;
        if (agent != null) {
            agentName = agent.toString();
        }
        addLabeledValue(infoBorder, "agent", "Agent", agentName, true);
        BuildAgent buildAgent = build.getBuildAgent();
        String buildAgentName = (buildAgent != null) ? buildAgent.toString() : null;
        addLabeledValue(infoBorder, "buildAgent", "Build Agent", buildAgentName, true);
        addLabeledValue(infoBorder, "started", "Started", build.getStarted(), true);

        Duration duration = Duration.milliseconds(build.getDurationMillis());
        addLabeledValue(infoBorder, "duration", "Duration", duration.toString(), true);
        addLabeledValue(infoBorder, "principal", "Principal", build.getPrincipal(), true);
        addLabeledValue(infoBorder, "artifactoryPrincipal", "Artifactory Principal", build.getArtifactoryPrincipal(),
                true);
        infoBorder.add(new Label("urlLabel", "URL:"));

        String url = build.getUrl();
        if (url == null) {
            url = "";
        }
        infoBorder.add(new ExternalLink("url", url, url));
        addLabeledValue(infoBorder, "parentBuildId", "Parent Build ID", build.getParentBuildId(),
                StringUtils.isNotBlank(build.getParentBuildId()));
        addLabeledValue(infoBorder, "parentBuildName", "Parent Build Name", build.getParentName(),
                StringUtils.isNotBlank(build.getParentName()));
        addLabeledValue(infoBorder, "parentBuildNumber", "Parent Build Number", build.getParentNumber(),
                StringUtils.isNotBlank(build.getParentNumber()));

        addIssueTrackerInformation(infoBorder, build.getIssues());

        addBintrayButton(infoBorder, build);
    }

    private void addBintrayButton(FieldSetBorder infoBorder, final Build build) {
        boolean showPushToBintray = isShowPushToBintray();
        if (!showPushToBintray) {
            infoBorder.add(new WebMarkupContainer("pushToBintray").setVisible(false));
        } else {
            infoBorder.add(new ModalShowLink("pushToBintray", "") {
                @Override
                protected BaseModalPanel getModelPanel() {
                    return new BintrayBuildPanel(build);
                }

                @Override
                public void onClick(AjaxRequestTarget target) {
                    if (!bintrayService.isUserHasBintrayAuth()) {
                        String profilePagePath = WicketUtils.absoluteMountPathForPage(ProfilePage.class);
                        String message = "You do not have Bintray credentials configured, please configure them from your <a href=\"" + profilePagePath + "\">profile page</a>.";
                        getPage().error(new UnescapedFeedbackMessage(message));
                    } else {
                        super.onClick(target);
                    }
                }
            });
        }
    }

    private boolean isShowPushToBintray() {
        CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
        boolean userExists = !addons.isAolAdmin() && !userGroupService.currentUser().isTransientUser();
        boolean anonymousUser = authorizationService.isAnonymous();
        boolean hideUploads = ConstantValues.bintrayUIHideUploads.getBoolean();
        boolean offlineMode = centralConfigService.getDescriptor().isOfflineMode();
        return !anonymousUser && !hideUploads && !offlineMode && userExists;
    }

    private void addIssueTrackerInformation(FieldSetBorder infoBorder, Issues issues) {
        WebMarkupContainer issueTrackerContainer = new WebMarkupContainer("issueTracker");
        infoBorder.add(issueTrackerContainer);

        if (issues != null) {
            IssueTracker tracker = issues.getTracker();
            if (tracker != null) {
                String trackerName = tracker.getName();
                if (StringUtils.isNotBlank(trackerName)) {
                    StringBuilder trackerInfoBuilder = new StringBuilder(trackerName);
                    String version = tracker.getVersion();
                    if (StringUtils.isNotBlank(version)) {
                        trackerInfoBuilder.append("/").append(version);
                    }
                    issueTrackerContainer.replaceWith(new LabeledValue("issueTracker", "Issue Tracker: ",
                            trackerInfoBuilder.toString()));
                }
            }
        }
    }

    /**
     * Adds a labeled value of the given details
     *
     * @param infoBorder Border to add the label to
     * @param id         ID to assign to the labeled value
     * @param label      Textual label
     * @param labelValue Textual value
     * @param visible
     */
    private void addLabeledValue(FieldSetBorder infoBorder, String id, String label, String labelValue,
            boolean visible) {
        LabeledValue value = new LabeledValue(id, label + ": ", (labelValue != null) ? labelValue : "");
        value.setVisible(visible);
        infoBorder.add(value);
    }

    /**
     * Adds the save search results panel
     *
     * @param build Build to use as file resource
     */
    private void addSaveSearchResultsPanel(Build build) {
        SearchAddon searchAddon = addonsManager.addonByType(SearchAddon.class);
        //Make the search addon the requesting, so if it is disabled, it's because of the search
        SaveSearchResultsPanel panel = searchAddon.getBuildSearchResultsPanel(AddonType.SEARCH, build);
        panel.init();
        add(panel);
    }
}