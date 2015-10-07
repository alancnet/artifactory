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

package org.artifactory.webapp.wicket.application.sitemap;

import org.apache.wicket.Page;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BlackDuckWebAddon;
import org.artifactory.addon.wicket.LicensesWebAddon;
import org.artifactory.addon.wicket.PropertiesWebAddon;
import org.artifactory.addon.wicket.SearchAddon;
import org.artifactory.addon.wicket.WebApplicationAddon;
import org.artifactory.addon.wicket.WebstartWebAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.common.wicket.model.sitemap.MenuNodeVisitor;
import org.artifactory.common.wicket.model.sitemap.SiteMap;
import org.artifactory.common.wicket.model.sitemap.SiteMapBuilder;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.webapp.wicket.page.admin.AdminPage;
import org.artifactory.webapp.wicket.page.browse.home.ArtifactsHomePage;
import org.artifactory.webapp.wicket.page.browse.simplebrowser.root.SimpleBrowserRootPage;
import org.artifactory.webapp.wicket.page.browse.treebrowser.BrowseRepoPage;
import org.artifactory.webapp.wicket.page.build.page.BuildBrowserRootPage;
import org.artifactory.webapp.wicket.page.deploy.DeployArtifactPage;
import org.artifactory.webapp.wicket.page.deploy.fromzip.DeployFromZipPage;
import org.artifactory.webapp.wicket.page.home.HomePage;
import org.artifactory.webapp.wicket.page.home.settings.ivy.IvySettingsPage;
import org.artifactory.webapp.wicket.page.home.settings.ivy.gradle.GradleBuildScriptPage;
import org.artifactory.webapp.wicket.page.home.settings.maven.MavenSettingsPage;
import org.artifactory.webapp.wicket.page.search.archive.ArchiveSearchPage;
import org.artifactory.webapp.wicket.page.search.artifact.ArtifactSearchPage;
import org.artifactory.webapp.wicket.page.search.bintray.BintraySearchPage;
import org.artifactory.webapp.wicket.page.search.checksum.ChecksumSearchPage;
import org.artifactory.webapp.wicket.page.search.gavc.GavcSearchPage;

import java.util.Iterator;

/**
 * @author Yoav Aharoni
 */
public class ArtifactorySiteMapBuilder extends SiteMapBuilder {

    @SpringBean
    private AddonsManager addons;

    @Override
    public void buildSiteMap() {
        WebApplicationAddon applicationAddon = addons.addonByType(WebApplicationAddon.class);

        SiteMap siteMap = getSiteMap();
        MenuNode root = new MenuNode("Artifactory", Page.class);
        siteMap.setRoot(root);

        final MenuNode homePage = new HomeMenuNode(applicationAddon.getHomePage());
        root.addChild(homePage);

        MenuNode homeGroup = new OpenedMenuNode("Home");
        homePage.addChild(homeGroup);

        MenuNode welcomePage = new MenuNode("Welcome", HomePage.class);
        homeGroup.addChild(welcomePage);

        MenuNode settingsGroup = new OpenedMenuNode("Client Settings");
        homeGroup.addChild(settingsGroup);
        settingsGroup.addChild(new MenuNode("Maven Settings", MavenSettingsPage.class));
        settingsGroup.addChild(new MenuNode("Gradle Build Script", GradleBuildScriptPage.class));
        settingsGroup.addChild(new MenuNode("Ivy Settings", IvySettingsPage.class));

        MenuNode browseRepoPage = new ArtifactsPageNode("Artifacts", ArtifactsHomePage.class);
        root.addChild(browseRepoPage);

        MenuNode browseGroup = new OpenedMenuNode("Browse");
        browseRepoPage.addChild(browseGroup);

        browseGroup.addChild(new ArtifactsPageNode("Tree Browser", BrowseRepoPage.class));
        browseGroup.addChild(new ArtifactsPageNode("Simple Browser", SimpleBrowserRootPage.class));
        SearchAddon searchAddon = addons.addonByType(SearchAddon.class);
        browseGroup.addChild(searchAddon.getBrowserSearchMenuNode());
        browseGroup.addChild(new ArtifactsPageNode("Builds", BuildBrowserRootPage.class));

        MenuNode searchGroup = new OpenedMenuNode("Search");
        browseRepoPage.addChild(searchGroup);
        searchGroup.addChild(new ArtifactsPageNode("Quick Search", ArtifactSearchPage.class));
        searchGroup.addChild(new ArtifactsPageNode("Class Search", ArchiveSearchPage.class));
        searchGroup.addChild(new ArtifactsPageNode("GAVC Search", GavcSearchPage.class));
        PropertiesWebAddon propertiesWebAddon = addons.addonByType(PropertiesWebAddon.class);
        searchGroup.addChild(propertiesWebAddon.getPropertySearchMenuNode("Property Search"));
        searchGroup.addChild(new ArtifactsPageNode("Checksum Search", ChecksumSearchPage.class));
        searchGroup.addChild(new BintraySearchPageNode());
        DeployArtifactPageNode deployPage = new DeployArtifactPageNode(DeployArtifactPage.class, "Deploy");
        root.addChild(deployPage);
        MenuNode deployGroup = new OpenedMenuNode("Deploy");
        deployPage.addChild(deployGroup);
        deployGroup.addChild(new DeployArtifactPageNode(DeployArtifactPage.class, "Single Artifact"));
        deployGroup.addChild(new DeployArtifactPageNode(DeployFromZipPage.class, "Artifacts Bundle"));

        MenuNode adminPage = new AdminPageNode("Admin");
        root.addChild(adminPage);
        LicensesWebAddon licensesWebAddon = addons.addonByType(LicensesWebAddon.class);
        BlackDuckWebAddon blackDuckWebAddon = addons.addonByType(BlackDuckWebAddon.class);
        MenuNode adminConfiguration = applicationAddon.getConfigurationMenuNode(propertiesWebAddon, licensesWebAddon,
                blackDuckWebAddon);
        adminPage.addChild(adminConfiguration);

        WebstartWebAddon webstartAddon = addons.addonByType(WebstartWebAddon.class);
        MenuNode securityConfiguration = applicationAddon.getSecurityMenuNode(webstartAddon);
        adminPage.addChild(securityConfiguration);

        MenuNode adminServices = applicationAddon.getServicesMenuNode();
        adminPage.addChild(adminServices);

        MenuNode adminImportExport = applicationAddon.getImportExportMenuNode();
        adminPage.addChild(adminImportExport);

        MenuNode adminAdvanced = applicationAddon.getAdvancedMenuNode();
        adminPage.addChild(adminAdvanced);

        siteMap.visitPageNodes(new RemoveUnauthorizedNodesVisitor());
    }

    private static class DeployArtifactPageNode extends SecuredPageNode {
        private DeployArtifactPageNode(Class<? extends Page> pageClass, String name) {
            super(pageClass, name);
        }

        @Override
        public boolean isEnabled() {
            if (!getAuthorizationService().hasPermission(ArtifactoryPermission.DEPLOY)) {
                return false;
            }

            if (getAddons().lockdown()) {
                return false;
            }

            return getAuthorizationService().canDeployToLocalRepository();
        }
    }

    private static class ArtifactsPageNode extends SecuredPageNode {
        private ArtifactsPageNode(String name, Class<? extends Page> pageClass) {
            super(pageClass, name);
        }

        @Override
        public boolean isEnabled() {
            if (getAddons().lockdown()) {
                return false;
            }
            AuthorizationService authorizationService = getAuthorizationService();
            if (authorizationService.isAnonAccessEnabled() || authorizationService.hasPermission(
                    ArtifactoryPermission.READ)) {
                return true;
            }
            return getAuthorizationService().canDeployToLocalRepository();
        }
    }

    private static class BintraySearchPageNode extends ArtifactsPageNode {
        private BintraySearchPageNode() {
            super("Remote Search", BintraySearchPage.class);
        }

        @Override
        public String getCssClass() {
            return "bintray-search";
        }

    }

    private static class AdminPageNode extends SecuredPageNode {
        private AdminPageNode(String name) {
            super(AdminPage.class, name);
        }

        @Override
        public boolean isEnabled() {
            // allow only admins or users with admin permissions on a permission target
            return getAddonsWebManager().isAdminPageAccessible();
        }
    }

    private static class OpenedMenuNode extends MenuNode {
        public OpenedMenuNode(String name) {
            super(name);
        }

        @Override
        public Boolean isOpened() {
            return true;
        }
    }

    private class RemoveUnauthorizedNodesVisitor implements MenuNodeVisitor {
        @Override
        public void visit(MenuNode node, Iterator<MenuNode> iterator) {
            // check if page instantiation is allowed
            Class<? extends Page> pageClass = node.getPageClass();
            if (pageClass != null) {
                WebApplicationAddon applicationAddon = addons.addonByType(WebApplicationAddon.class);
                boolean instantiationAuthorized = applicationAddon.isVisibilityAuthorized(pageClass);
                if (!instantiationAuthorized) {
                    iterator.remove();
                }
            }
        }
    }

    private static class HomeMenuNode extends MenuNode {
        public HomeMenuNode(Class<? extends Page> pageClass) {
            super("Home", pageClass);
        }

        @Override
        public String getCssClass() {
            return "HomePage";
        }
    }
}