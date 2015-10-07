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

package org.artifactory.webapp.wicket.page.build.panel;

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BlackDuckWebAddon;
import org.artifactory.addon.wicket.BuildAddon;
import org.artifactory.addon.wicket.LicensesWebAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.behavior.RenderJavaScript;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.webapp.wicket.page.build.tabs.BuildEnvironmentTabPanel;
import org.artifactory.webapp.wicket.page.build.tabs.BuildGeneralInfoTabPanel;
import org.artifactory.webapp.wicket.page.build.tabs.BuildInfoJsonTabPanel;
import org.artifactory.webapp.wicket.page.build.tabs.IssuesTabPanel;
import org.artifactory.webapp.wicket.page.build.tabs.PublishedModulesTabPanel;
import org.artifactory.webapp.wicket.page.build.tabs.ReleaseHistoryTabPanel;
import org.artifactory.webapp.wicket.panel.tabbed.StyledTabbedPanel;
import org.artifactory.webapp.wicket.panel.tabbed.tab.BaseTab;
import org.artifactory.webapp.wicket.panel.tabbed.tab.DisabledTab;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Module;

import java.util.List;

/**
 * Displays different tabs containing the information of a selected build
 *
 * @author Noam Y. Tenne
 */
public class BuildTabbedPanel extends TitledPanel {

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AuthorizationService authorizationService;

    private Build build;
    private Module module;

    /**
     * Main constructor
     *
     * @param id    ID to assign to the panel
     * @param build Build to display
     */
    public BuildTabbedPanel(String id, Build build) {
        this(id, build, null);
    }

    /**
     * Module constructor - Use when needing to substitue the published modules tab panel with a specific module panel
     *
     * @param id     ID to assign to the panel
     * @param build  Build to display
     * @param module Module to display
     */
    public BuildTabbedPanel(String id, Build build, Module module) {
        super(id);
        this.build = build;
        this.module = module;
        addTabbedPanel();
    }

    @Override
    public String getTitle() {
        if (module == null) {
            return String.format("Build #%s", build.getNumber());
        }
        return String.format("Module %s", module.getId());
    }

    /**
     * Adds the tabbed panel
     */
    private void addTabbedPanel() {

        //Determine if a specific module was supplied
        final boolean targetSpecificModule = module != null;

        //Check if the user can deploy on any local repos
        final boolean hasDeployOnLocal = authorizationService.canDeployToLocalRepository();

        List<ITab> tabList = Lists.newArrayList();

        tabList.add(getGeneralInfoTab("General Build Info", hasDeployOnLocal));

        if (!targetSpecificModule) {
            tabList.add(new AbstractTab(Model.of("Published Modules")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new PublishedModulesTabPanel(panelId, build);
                }
            });
        } else {
            BuildAddon buildAddon = addonsManager.addonByType(BuildAddon.class);
            ITab tab = buildAddon.getModuleInfoTab(build, module);
            tabList.add(tab);
        }

        tabList.add(getEnvironmentTab("Environment", hasDeployOnLocal));

        tabList.add(getIssuesTab("Issues", hasDeployOnLocal));


        BlackDuckWebAddon blackDuckWebAddon = addonsManager.addonByType(BlackDuckWebAddon.class);
        ITab governanceTab = blackDuckWebAddon.getBuildInfoTab("Governance", build, hasDeployOnLocal);

        if (blackDuckWebAddon.shouldShowLicensesAddonTab(governanceTab, build)) {
            LicensesWebAddon licensesAddon = addonsManager.addonByType(LicensesWebAddon.class);
            tabList.add(licensesAddon.getLicensesInfoTab("Licenses", build, hasDeployOnLocal));
        }

        tabList.add(governanceTab);

        BuildAddon buildAddon = addonsManager.addonByType(BuildAddon.class);
        ITab tab = buildAddon.getBuildDiffTab("Diff", build, hasDeployOnLocal);
        tabList.add(tab);

        tabList.add(getReleaseHistoryTab("Release History", hasDeployOnLocal));

        tabList.add(getJsonTab("Build Info JSON", hasDeployOnLocal));

        StyledTabbedPanel tabbedPanel = new StyledTabbedPanel("tabbedPanel", tabList);
        tabbedPanel.add(new RenderJavaScript("Tab.fixHeight();"));
        /**
         * If the user has no deployment rights on local repos or if a specific module was supplied, focus on the
         * modules tab
         */
        if (!hasDeployOnLocal || targetSpecificModule) {
            tabbedPanel.setSelectedTab(1);
        }

        add(tabbedPanel);
    }

    /**
     * Returns the general info tab
     *
     * @param title   Title to give to the tab
     * @param enabled Indicates the state of the tab
     * @return General info tab
     */
    private ITab getGeneralInfoTab(String title, boolean enabled) {
        if (enabled) {
            return new BaseTab(title) {
                @Override
                public Panel getPanel(String panelId) {
                    return new BuildGeneralInfoTabPanel(panelId, build);
                }
            };
        } else {
            return getDisabledTab(title);
        }
    }

    /**
     * Returns the environment tab
     *
     * @param title   Title to give to the tab
     * @param enabled Indicates the state of the tab
     * @return Environment tab
     */
    private ITab getEnvironmentTab(String title, boolean enabled) {
        if (enabled) {
            return new BaseTab(title) {
                @Override
                public Panel getPanel(String panelId) {
                    return new BuildEnvironmentTabPanel(panelId, build);
                }
            };
        } else {
            return getDisabledTab(title);
        }
    }

    private ITab getIssuesTab(String title, boolean enabled) {
        if (enabled) {
            return new BaseTab(title) {
                @Override
                public Panel getPanel(String panelId) {
                    return new IssuesTabPanel(panelId, build);
                }
            };
        } else {
            return getDisabledTab(title);
        }
    }

    /**
     * Returns the build release history tab
     *
     * @param title   Title to give to the tab
     * @param enabled Indicates the state of the tab
     * @return Release history tab
     */
    private ITab getReleaseHistoryTab(String title, boolean enabled) {
        if (enabled) {
            return new BaseTab(title) {
                @Override
                public Panel getPanel(String panelId) {
                    return new ReleaseHistoryTabPanel(panelId, build);
                }
            };
        } else {
            return getDisabledTab(title);
        }
    }

    /**
     * Returns the info JSON tab
     *
     * @param title   Title to give to the tab
     * @param enabled Indicates the state of the tab
     * @return Info JSON tab
     */
    private ITab getJsonTab(String title, boolean enabled) {
        if (enabled) {
            return new BaseTab(title) {
                @Override
                public Panel getPanel(String panelId) {
                    return new BuildInfoJsonTabPanel(panelId, build);
                }
            };
        } else {
            return getDisabledTab(title);
        }
    }

    /**
     * Returns the default disabled tab
     *
     * @param title Title to give to the tab
     * @return Disabled tab
     */
    private ITab getDisabledTab(String title) {
        return new DisabledTab(title);
    }
}