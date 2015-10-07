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

package org.artifactory.addon.wicket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.addon.AddonInfo;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.p2.P2Repository;
import org.artifactory.addon.p2.P2WebAddon;
import org.artifactory.addon.wicket.disabledaddon.AddonNeededBehavior;
import org.artifactory.addon.wicket.disabledaddon.DisabledAddonBehavior;
import org.artifactory.addon.wicket.disabledaddon.DisabledAddonHelpBubble;
import org.artifactory.addon.wicket.disabledaddon.DisabledAddonMenuNode;
import org.artifactory.addon.wicket.disabledaddon.DisabledAddonTab;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.config.VersionInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.version.VersionHolder;
import org.artifactory.api.version.VersionInfoService;
import org.artifactory.build.ArtifactoryBuildArtifact;
import org.artifactory.build.BuildRun;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.border.TitledBorderBehavior;
import org.artifactory.common.wicket.behavior.collapsible.DisabledCollapsibleBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.CreateUpdatePanel;
import org.artifactory.common.wicket.component.PlaceHolder;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.BaseTitledLink;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.modal.panel.EditValueButtonRefreshBehavior;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.columns.BooleanColumn;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.common.wicket.property.PropertyItem;
import org.artifactory.common.wicket.util.SetEnableVisitor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.group.LdapGroupPopulatorStrategies;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.action.DeleteAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.artifactory.webapp.wicket.page.base.BasePage;
import org.artifactory.webapp.wicket.page.base.EditProfileLink;
import org.artifactory.webapp.wicket.page.base.LoginLink;
import org.artifactory.webapp.wicket.page.base.LogoutLink;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.BaseBuildsTabPanel;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.actionable.BuildDependencyActionableItem;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.build.actionable.BuildTabActionableItem;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.properties.PropertiesPanel;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleArtifactActionableItem;
import org.artifactory.webapp.wicket.page.build.actionable.ModuleDependencyActionableItem;
import org.artifactory.webapp.wicket.page.build.tabs.BuildSearchResultsPanel;
import org.artifactory.webapp.wicket.page.build.tabs.DisabledModuleInfoTabPanel;
import org.artifactory.webapp.wicket.page.build.tabs.diff.DisabledBuildDiffTabPanel;
import org.artifactory.webapp.wicket.page.config.HaNotConfiguredPage;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;
import org.artifactory.webapp.wicket.page.config.advanced.AdvancedCentralConfigPage;
import org.artifactory.webapp.wicket.page.config.advanced.AdvancedSecurityConfigPage;
import org.artifactory.webapp.wicket.page.config.advanced.MaintenancePage;
import org.artifactory.webapp.wicket.page.config.advanced.SystemInfoPage;
import org.artifactory.webapp.wicket.page.config.advanced.storage.StorageSummaryPage;
import org.artifactory.webapp.wicket.page.config.bintray.BintrayConfigPage;
import org.artifactory.webapp.wicket.page.config.general.BaseCustomizingPanel;
import org.artifactory.webapp.wicket.page.config.general.CustomizingPanel;
import org.artifactory.webapp.wicket.page.config.general.GeneralConfigPage;
import org.artifactory.webapp.wicket.page.config.layout.LayoutListPanel;
import org.artifactory.webapp.wicket.page.config.layout.RepoLayoutPage;
import org.artifactory.webapp.wicket.page.config.license.LicensePage;
import org.artifactory.webapp.wicket.page.config.mail.MailConfigPage;
import org.artifactory.webapp.wicket.page.config.proxy.ProxyConfigPage;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.RepositoryConfigPage;
import org.artifactory.webapp.wicket.page.config.security.LdapGroupListPanel;
import org.artifactory.webapp.wicket.page.config.security.LdapsListPage;
import org.artifactory.webapp.wicket.page.config.security.LdapsListPanel;
import org.artifactory.webapp.wicket.page.config.security.general.SecurityGeneralConfigPage;
import org.artifactory.webapp.wicket.page.config.services.BackupsListPage;
import org.artifactory.webapp.wicket.page.config.services.IndexerConfigPage;
import org.artifactory.webapp.wicket.page.home.HomePage;
import org.artifactory.webapp.wicket.page.home.addon.AddonsInfoPanel;
import org.artifactory.webapp.wicket.page.home.settings.modal.editable.BaseSettingsProvisioningBorder;
import org.artifactory.webapp.wicket.page.importexport.repos.ImportExportReposPage;
import org.artifactory.webapp.wicket.page.importexport.system.ImportExportSystemPage;
import org.artifactory.webapp.wicket.page.logs.SystemLogsPage;
import org.artifactory.webapp.wicket.page.search.ArtifactSaveSearchResultsPanel;
import org.artifactory.webapp.wicket.page.search.LimitlessCapableSearcher;
import org.artifactory.webapp.wicket.page.search.SaveSearchResultsPanel;
import org.artifactory.webapp.wicket.page.security.acl.AclsPage;
import org.artifactory.webapp.wicket.page.security.group.GroupsPage;
import org.artifactory.webapp.wicket.page.security.user.UsersPage;
import org.artifactory.webapp.wicket.panel.export.ExportResultsPanel;
import org.artifactory.webapp.wicket.panel.tabbed.tab.BaseTab;
import org.artifactory.webapp.wicket.util.ItemCssClass;
import org.artifactory.webapp.wicket.util.validation.UriValidator;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.BaseBuildFileBean;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildRetention;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Module;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.dependency.BuildPatternArtifactsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.artifactory.addon.AddonType.*;

/**
 * Default implementation of the addons interface. Represents a normal execution of artifactory.
 * <p/>
 * <strong>NOTE!</strong> Do not create anonymous or non-static inner classes in addon
 *
 * @author freds
 * @author Yossi Shaul
 */
@org.springframework.stereotype.Component
public final class WicketAddonsImpl implements CoreAddons, WebApplicationAddon, PropertiesWebAddon, SearchAddon,
        WatchAddon, WebstartWebAddon, HttpSsoAddon, CrowdWebAddon, SamlAddon, SamlWebAddon, LdapGroupWebAddon,
        BuildAddon, LicensesWebAddon, LayoutsWebAddon, FilteredResourcesWebAddon, ReplicationWebAddon, YumWebAddon,
        P2WebAddon, NuGetWebAddon, BlackDuckWebAddon, GemsWebAddon, HaWebAddon, NpmWebAddon, DebianWebAddon,
        PypiWebAddon, DockerWebAddon, VcsWebAddon, BowerWebAddon, VagrantWebAddon, GitLfsWebAddon, PluginsWebAddon {
    private static final Logger log = LoggerFactory.getLogger(WicketAddonsImpl.class);

    @Autowired
    CentralConfigService centralConfigService;

    private static String buildLatestVersionLabel(VersionHolder latestVersion) {
        return String.format("(latest release is <a href=\"%s\" target=\"_blank\">%s</a>)",
                latestVersion.getDownloadUrl(), latestVersion.getVersion());
    }

    private static void disableAll(MarkupContainer container) {
        container.setEnabled(false);
        container.visitChildren(new SetEnableVisitor(false));
    }

    @Override
    public String getPageTitle(BasePage page) {
        String serverName = getCentralConfig().getServerName();
        return "Artifactory@" + serverName + " :: " + page.getPageName();
    }

    @Override
    public MenuNode getSecurityMenuNode(WebstartWebAddon webstartAddon) {
        MenuNode security = new MenuNode("Security");
        security.addChild(new MenuNode("General", SecurityGeneralConfigPage.class));
        security.addChild(new MenuNode("Users", UsersPage.class));
        security.addChild(new MenuNode("Groups", GroupsPage.class));
        security.addChild(new MenuNode("Permissions", AclsPage.class));
        security.addChild(new MenuNode("LDAP Settings", LdapsListPage.class));
        CrowdWebAddon crowdWebAddon = getAddonsManager().addonByType(CrowdWebAddon.class);
        security.addChild(crowdWebAddon.getCrowdAddonMenuNode("Crowd Integration"));
        SamlWebAddon samlWebAddon = getAddonsManager().addonByType(SamlWebAddon.class);
        security.addChild(samlWebAddon.getSamlAddonMenuNode("SAML Integration"));
        HttpSsoAddon httpSsoAddon = getAddonsManager().addonByType(HttpSsoAddon.class);
        security.addChild(httpSsoAddon.getHttpSsoMenuNode("HTTP SSO"));
        security.addChild(webstartAddon.getKeyPairMenuNode());
        return security;
    }

    @Override
    public MenuNode getConfigurationMenuNode(PropertiesWebAddon propertiesWebAddon, LicensesWebAddon licensesWebAddon,
            BlackDuckWebAddon blackDuckWebAddon) {
        MenuNode adminConfiguration = new MenuNode("Configuration");
        adminConfiguration.addChild(new MenuNode("General", GeneralConfigPage.class));
        adminConfiguration.addChild(new MenuNode("Repositories", RepositoryConfigPage.class));
        adminConfiguration.addChild(new MenuNode("Repository Layouts", RepoLayoutPage.class));
        AddonsManager addonsManager = getAddonsManager();
        LicensesWebAddon licensesAddon = addonsManager.addonByType(LicensesWebAddon.class);
        adminConfiguration.addChild(licensesAddon.getLicensesMenuNode("Licenses"));
        adminConfiguration.addChild(blackDuckWebAddon.getBlackDuckMenuNode("Black Duck"));
        adminConfiguration.addChild(propertiesWebAddon.getPropertySetsPage("Property Sets"));
        adminConfiguration.addChild(new MenuNode("Proxies", ProxyConfigPage.class));
        adminConfiguration.addChild(new MenuNode("Mail", MailConfigPage.class));
        HaWebAddon haWebAddon = addonsManager.addonByType(HaWebAddon.class);
        adminConfiguration.addChild(haWebAddon.getHaConfigPage("High Availability"));
        if (!ConstantValues.bintrayUIHideUploads.getBoolean()) {
            adminConfiguration.addChild(new MenuNode("Bintray", BintrayConfigPage.class));
        }
        if (!(addonsManager instanceof OssAddonsManager)) {
            adminConfiguration.addChild(new MenuNode("Register Pro", LicensePage.class));
        }
        return adminConfiguration;
    }

    @Override
    public Label getUrlBaseLabel(String id) {
        return new Label(id, "Custom URL Base");
    }

    @Override
    public TextField getUrlBaseTextField(String id) {
        TextField<String> urlBaseTextField = new TextField<>(id);
        urlBaseTextField.add(new UriValidator("http", "https"));
        return urlBaseTextField;
    }

    @Override
    public HelpBubble getUrlBaseHelpBubble(String id) {
        return new SchemaHelpBubble(id);
    }

    @Override
    public IFormSubmittingComponent getLoginLink(String wicketId, Form form) {
        return new DefaultLoginLink(wicketId, "Log In", form);
    }

    @Override
    public LogoutLink getLogoutLink(String wicketId) {
        return new LogoutLink(wicketId, "Log Out");
    }

    @Override
    public AbstractLink getLoginLink(String wicketId) {
        return new LoginLink(wicketId, "Log In");
    }

    @Override
    public boolean isAutoRedirectToSamlIdentityProvider() {
        return false;
    }

    @Override
    public boolean isSamlEnabled() {
        return false;
    }

    @Override
    public String getSamlLoginIdentityProviderUrl() {
        return null;
    }

    @Override
    public AbstractLink getProfileLink(String wicketId) {
        return new EditProfileLink("profilePage");
    }

    @Override
    public String resetPassword(String userName, String remoteAddress, String resetPageUrl) {
        return ContextHelper.get().beanForType(UserGroupService.class).
                resetPassword(userName, remoteAddress, resetPageUrl);
    }

    @Override
    public boolean isInstantiationAuthorized(Class componentClass) {
        if (componentClass.equals(LicensePage.class)) {
            AuthorizationService authorizationService = ContextHelper.get().getAuthorizationService();
            if (!authorizationService.isAdmin() && getAddonsManager().isLicenseInstalled()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isVisibilityAuthorized(Class componentClass) {
        return true;
    }

    @Override
    public Label getUptimeLabel(String wicketId) {
        long uptime = ContextHelper.get().getUptime();
        String uptimeStr = DurationFormatUtils.formatDuration(uptime, "d'd' H'h' m'm' s's'");
        Label uptimeLabel = new Label(wicketId, uptimeStr);
        //Only show uptime for admins
        if (!ContextHelper.get().getAuthorizationService().isAdmin()) {
            uptimeLabel.setVisible(false);
        }
        return uptimeLabel;
    }

    @Override
    public void addVersionInfo(WebMarkupContainer container, Map<String, String> headersMap) {
        WebMarkupContainer versioningInfo = new WebMarkupContainer("versioningInfo");

        versioningInfo.add(new Label("currentLabel", ConstantValues.artifactoryVersion.getString()));

        Label latestLabel = new Label("latestLabel", "");
        latestLabel.setEscapeModelStrings(false);   // to include a link easily...
        latestLabel.setOutputMarkupId(true);
        String latestWikiUrl = VersionHolder.VERSION_UNAVAILABLE.getWikiUrl();
        CentralConfigDescriptor configDescriptor = getCentralConfig().getDescriptor();
        if (ConstantValues.versionQueryEnabled.getBoolean() && !configDescriptor.isOfflineMode()) {
            // try to get the latest version from the cache with a non-blocking call
            VersionInfoService versionInfoService = ContextHelper.get().beanForType(VersionInfoService.class);
            VersionHolder latestVersion = versionInfoService.getLatestVersion(headersMap, true);
            latestWikiUrl = latestVersion.getWikiUrl();
            if (VersionInfoService.SERVICE_UNAVAILABLE.equals(latestVersion.getVersion())) {
                // send ajax refresh in 5 second and update the latest version with the result
                latestLabel.add(new UpdateNewsFromCache());
            } else {
                latestLabel.setDefaultModelObject(buildLatestVersionLabel(latestVersion));
            }
        }
        versioningInfo.add(latestLabel);

        ExternalLink wikiLink = new ExternalLink("wikiLink", latestWikiUrl);
        versioningInfo.add(wikiLink);

        container.add(versioningInfo);
    }

    @Override
    public SaveSearchResultsPanel getSaveSearchResultsPanel(String wicketId, IModel model,
            LimitlessCapableSearcher limitlessCapableSearcher) {
        SaveSearchResultsPanel panel = new ArtifactSaveSearchResultsPanel(wicketId, model, SEARCH);
        panel.setEnabled(false);
        return panel;
    }

    @Override
    public SaveSearchResultsPanel getBuildSearchResultsPanel(AddonType requestingAddon, Build build) {
        return new BuildSearchResultsPanel(requestingAddon, build);
    }

    @Override
    public Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfosWithFallback(Build build) {
        return Sets.newHashSet();
    }

    @Override
    public Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build) {
        return Sets.newHashSet();
    }

    @Override
    public Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build) {
        return Maps.newHashMap();
    }

    @Override
    public void renameBuildNameProperty(String from,
            String to) {
    }

    @Override
    public void discardOldBuildsByDate(String buildName, BuildRetention buildRetention,
            BasicStatusHolder multiStatusHolder) {
        // nop
    }

    @Override
    public void discardOldBuildsByCount(String buildName, BuildRetention discard, BasicStatusHolder multiStatusHolder) {
        // nop
    }

    @Override
    public BuildPatternArtifacts getBuildPatternArtifacts(
            @Nonnull BuildPatternArtifactsRequest buildPatternArtifactsRequest, String servletContextUrl) {
        return new BuildPatternArtifacts();
    }

    @Override
    public Map<FileInfo, String> getBuildArtifacts(BuildArtifactsRequest buildArtifactsRequest) {
        return null;
    }

    @Override
    public File getBuildArtifactsArchive(BuildArtifactsRequest buildArtifactsRequest) {
        return null;
    }

    @Override
    public BuildsDiff getBuildsDiff(Build firstBuild, Build secondBuild, String baseStorageInfoUri) {
        return null;
    }

    @Override
    public FileInfo getFileBeanInfo(BaseBuildFileBean artifact, Build build) {
        return null;
    }

    @Override
    public String getSearchLimitDisclaimer() {
        return StringUtils.EMPTY;
    }

    @Override
    public ITab getPropertiesTabPanel(ItemInfo itemInfo) {
        return new DisabledAddonTab(Model.of("Properties"), PROPERTIES);
    }

    @Override
    public ITab getSearchPropertiesTabPanel(FolderInfo folderInfo, List<FileInfo> searchResults) {
        return getPropertiesTabPanel(folderInfo);
    }

    @Override
    public MenuNode getPropertySearchMenuNode(String nodeTitle) {
        return new DisabledAddonMenuNode(nodeTitle, PROPERTIES);
    }

    @Override
    public ITab getPropertySearchTabPanel(Page parent, String tabTitle) {
        return new DisabledAddonTab(Model.of(tabTitle), PROPERTIES);
    }

    @Override
    public MenuNode getPropertySetsPage(String nodeTitle) {
        return new DisabledAddonMenuNode(nodeTitle, PROPERTIES);
    }

    @Override
    public ITab getRepoConfigPropertySetsTab(String tabTitle, RealRepoDescriptor entity,
            List<PropertySet> propertySets) {
        return new DisabledAddonTab(Model.of(tabTitle), PROPERTIES);
    }

    @Override
    public BaseModalPanel getEditPropertyPanel(EditValueButtonRefreshBehavior refreshBehavior,
            PropertyItem propertyItem, List<PredefinedValue> values) {
        return null;
    }

    @Override
    public BaseModalPanel getChangeLicensePanel(EditValueButtonRefreshBehavior refreshBehavior, RepoPath path,
            List<LicenseInfo> currentValues) {
        return null;
    }

    @Override
    public Component getLicenseGeneralInfoPanel(RepoAwareActionableItem actionableItem) {
        return new WebMarkupContainer("licensesPanel").setVisible(false);
    }

    @Override
    public Component getTreeItemPropertiesPanel(String panelId, ItemInfo itemInfo) {
        return new DisabledPropertiesPanel(panelId, panelId);
    }

    @Override
    public Properties getProperties(RepoPath path) {
        return (Properties) InfoFactoryHolder.get().createProperties();
    }

    @Override
    public void removeProperties(RepoPath path, String propToDelete) {

    }

    @Override
    public Component getFilteredResourceCheckbox(String componentId, ItemInfo info) {
        return new StyledCheckbox(componentId).setTitle("").setEnabled(false).
                add(new DisabledAddonBehavior(FILTERED_RESOURCES));
    }

    @Override
    public Component getSettingsProvisioningBorder(String id, Form form, TextArea<String> content,
            String saveToFileName) {
        return new DisabledSettingsProvisioningBorder(id, form, content, saveToFileName);
    }

    @Override
    public Component getZipEntryActions(String wicketId, ActionableItem repoItem) {
        return new WebComponent(wicketId);
    }

    @Override
    public ItemAction getZipEntryDownloadAction() {
        ItemAction action = new NopAction();
        action.setEnabled(false);

        return action;
    }

    @Override
    public String getGeneratedSettingsUserCredentialsTemplate(boolean escape) {
        return "";
    }

    @Override
    public String getGeneratedSettingsUsernameTemplate() {
        return ContextHelper.get().getAuthorizationService().currentUsername();
    }

    @Override
    public String filterResource(Request request, Properties contextProperties, Reader reader) throws Exception {
        try {
            return IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public WebMarkupContainer getKeyPairContainer(String wicketId, String virtualRepoKey, boolean isCreate) {
        WebMarkupContainer container = new WebMarkupContainer(wicketId);
        DropDownChoice<Object> keyPairDropDown = new DropDownChoice<>("keyPair", Collections.emptyList());
        keyPairDropDown.setEnabled(false);
        keyPairDropDown.add(new DisabledAddonBehavior(WEBSTART));
        container.add(keyPairDropDown);
        container.add(new WebMarkupContainer("keyPairMessage"));
        container.add(new DisabledAddonHelpBubble("keyPair.help", WEBSTART));
        return container;
    }

    @Override
    public ItemAction getWatchAction(RepoPath itemRepoPath) {
        ItemAction action = new NopAction();
        action.setEnabled(false);

        return action;
    }

    @Override
    public ITab getWatchersTab(String tabTitle, RepoPath repoPath) {
        return new DisabledAddonTab(Model.of(tabTitle), WATCH);
    }

    @Override
    public MarkupContainer getWatchingSinceLabel(String labelId, RepoPath itemRepoPath) {
        return new PlaceHolder(labelId);
    }

    @Override
    public MarkupContainer getDirectlyWatchedPathPanel(String panelId, RepoPath itemRepoPath) {
        return new PlaceHolder(panelId);
    }

    @Override
    public MenuNode getHttpSsoMenuNode(String nodeName) {
        return new DisabledAddonMenuNode(nodeName, AddonType.SSO);
    }

    @Override
    public MenuNode getCrowdAddonMenuNode(String nodeName) {
        return new DisabledAddonMenuNode(nodeName, AddonType.SSO);
    }

    @Override
    public void testCrowdConnection(CrowdSettings crowdSettings) throws Exception {
        throw new UnsupportedOperationException("This feature requires the Crowd SSO addon.");
    }

    @Override
    public void logOffCrowd(HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    public Set findCrowdGroups(String username, CrowdSettings currentCrowdSettings) {
        return Sets.newHashSet();
    }

    @Override
    public Class<? extends WebPage> getSamlLoginRequestPageClass() {
        return null;
    }

    @Override
    public Class<? extends Page> getSamlLoginResponsePageClass() {
        return null;
    }

    @Override
    public Class<? extends WebPage> getSamlLogoutRequestPageClass() {
        return null;
    }

    @Override
    public FieldSetPanel getExportResultPanel(String panelId, ActionableItem item) {
        return new ExportResultsPanel(panelId, item);
    }

    @Override
    public BaseCustomizingPanel getCustomizingPanel(String id, IModel model) {
        return new CustomizingPanel(id, model);
    }

    @Override
    public WebMarkupContainer getAddonsInfoPanel(String panelId) {
        AddonsManager addonsManager = getAddonsManager();
        List<AddonInfo> installedAddons = addonsManager.getInstalledAddons(null);
        List<String> enabledAddonNames = addonsManager.getEnabledAddonNames();
        return new AddonsInfoPanel(panelId, installedAddons, enabledAddonNames.isEmpty());
    }
    @Override
    public ITab getBuildsTab(final RepoAwareActionableItem item) {
        return new DisabledBuildsTab(item);
    }

    @Override
    public ITab getModuleInfoTab(Build build, Module module) {
        return new DisabledPublishedTab();
    }

    @Override
    public ITab getBuildDiffTab(String title, Build build, boolean hasDeployOnLocal) {
        return new DisabledBuildDiffTab();
    }

    @Override
    public String getDeleteItemWarningMessage(org.artifactory.fs.ItemInfo item, String defaultMessage) {
        return defaultMessage;
    }

    @Override
    public String getDeleteVersionsWarningMessage(List<RepoPath> versionPaths, String defaultMessage) {
        return defaultMessage;
    }

    @Override
    public CreateUpdatePanel<LdapGroupSetting> getLdapGroupPanel(CreateUpdateAction createUpdateAction,
            LdapGroupSetting ldapGroupSetting, LdapGroupListPanel ldapGroupListPanel) {
        return null;
    }

    @Override
    public BooleanColumn<GroupInfo> addExternalGroupIndicator(BasicStatusHolder statusHolder) {
        return null;
    }

    @Override
    public TitledPanel getLdapGroupConfigurationPanel(String id) {
        return new DisabledLdapGroupListPanel("ldapGroupListPanel");
    }

    @Override
    public int importExternalGroupsToArtifactory(List ldapGroups, LdapGroupPopulatorStrategies strategy) {
        return 0;
    }

    @Override
    public Set refreshLdapGroupList(String userName, LdapGroupSetting ldapGroupSetting,
            BasicStatusHolder statusHolder) {
        return Sets.newHashSet();
    }

    @Override
    public Label getLdapActiveWarning(String wicketId) {
        Label warningLabel = new Label(wicketId);
        warningLabel.setVisible(false);
        return warningLabel;
    }

    @Override
    public WebMarkupContainer getLdapListPanel(String wicketId) {
        return new LdapsListPanel(wicketId);
    }

    @Override
    public void saveLdapSetting(MutableCentralConfigDescriptor configDescriptor, LdapSetting ldapSetting) {
        SecurityDescriptor securityDescriptor = configDescriptor.getSecurity();
        if (ldapSetting.isEnabled()) {
            List<LdapSetting> ldapSettings = securityDescriptor.getLdapSettings();
            for (LdapSetting setting : ldapSettings) {
                if (!ldapSetting.equals(setting)) {
                    setting.setEnabled(false);
                }
            }
        }
        LdapSetting setting = securityDescriptor.getLdapSettings(ldapSetting.getKey());
        if (setting != null) {
            List<LdapSetting> ldapSettings = securityDescriptor.getLdapSettings();
            int indexOfLdapSetting = ldapSettings.indexOf(ldapSetting);
            if (indexOfLdapSetting != -1) {
                ldapSettings.set(indexOfLdapSetting, ldapSetting);
            }
        }
    }

    @Override
    public Set<org.artifactory.fs.FileInfo> getBuildDependencyFileInfos(Build build, Set<String> scopes) {
        return Sets.newHashSet();
    }

    @Override
    public List<ModuleArtifactActionableItem> getModuleArtifactActionableItems(Build build, List<Artifact> artifacts) {
        return Lists.newArrayList();
    }

    @Override
    public List<ModuleDependencyActionableItem> populateModuleDependencyActionableItem(Build build,
            List<ModuleDependencyActionableItem> dependencies) {
        return Lists.newArrayList();
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    public MenuNode getAdvancedMenuNode() {
        MenuNode advancedConfiguration = new MenuNode("Advanced");
        advancedConfiguration.addChild(new MenuNode("System Info", SystemInfoPage.class));
        advancedConfiguration.addChild(new MenuNode("System Logs", SystemLogsPage.class));
        advancedConfiguration.addChild(new MenuNode("Maintenance", MaintenancePage.class));
        advancedConfiguration.addChild(new MenuNode("Storage Summary", StorageSummaryPage.class));
        advancedConfiguration.addChild(new MenuNode("Config Descriptor", AdvancedCentralConfigPage.class));
        advancedConfiguration.addChild(new MenuNode("Security Descriptor", AdvancedSecurityConfigPage.class));
        return advancedConfiguration;
    }

    @Override
    public MenuNode getBrowserSearchMenuNode() {
        return new DisabledAddonMenuNode("Search Results", SEARCH);
    }

    @Override
    public MenuNode getImportExportMenuNode() {
        MenuNode adminImportExport = new MenuNode("Import & Export");
        adminImportExport.addChild(new MenuNode("Repositories", ImportExportReposPage.class));
        adminImportExport.addChild(new MenuNode("System", ImportExportSystemPage.class));
        return adminImportExport;
    }

    @Override
    public MenuNode getKeyPairMenuNode() {
        return new DisabledAddonMenuNode("Signing Management", WEBSTART);
    }

    @Override
    public MenuNode getServicesMenuNode() {
        MenuNode services = new MenuNode("Services");
        services.addChild(new MenuNode("Backups", BackupsListPage.class));
        services.addChild(new MenuNode("Indexer", IndexerConfigPage.class));
        return services;
    }

    @Override
    public MenuNode getLicensesMenuNode(String nodeName) {
        return new DisabledAddonMenuNode(nodeName, AddonType.LICENSES);
    }

    @Override
    public ITab getLicensesInfoTab(String title, Build build, boolean hasDeployOnLocal) {
        return new DisabledAddonTab(Model.of(title), AddonType.LICENSES);
    }

    @Override
    public MenuNode getBlackDuckMenuNode(String nodeName) {
        return new DisabledAddonMenuNode(nodeName, AddonType.BLACKDUCK);
    }

    @Override
    public Component getBlackDuckLicenseGeneralInfoPanel(RepoAwareActionableItem actionableItem) {
        LicensesWebAddon licensesWebAddon = getAddonsManager().addonByType(LicensesWebAddon.class);
        return licensesWebAddon.getLicenseGeneralInfoPanel(actionableItem);
    }

    @Override
    public String getSearchResultsPageAbsolutePath(String resultToSelect) {
        return new StringBuilder(RequestUtils.getWicketServletContextUrl()).append("/").
                append(HttpUtils.WEBAPP_URL_PATH_PREFIX).toString();
    }

    @Override
    public MenuNode getHaConfigPage(String nodeName) {
        if (getAddonsManager() instanceof OssAddonsManager) {
            return new DisabledAddonMenuNode(nodeName, AddonType.HA);
        } else if (getAddonsManager().isHaLicensed()) {
            return new MenuNode(nodeName, HaNotConfiguredPage.class);
        } else {
            return null;
        }
    }

    @Override
    public String getVersionInfo() {
        VersionInfo versionInfo = getCentralConfig().getVersionInfo();
        AddonsManager addonsManager = getAddonsManager();
        String product = addonsManager.getProductName();
        return format("%s %s (rev. %s)", product, versionInfo.getVersion(), versionInfo.getRevision());
    }

    @Override
    public String getListBrowsingVersion() {
        VersionInfo versionInfo = centralConfigService.getVersionInfo();
        return format("Artifactory/%s", versionInfo.getVersion());
    }

    @Override
    public String getArtifactoryUrl() {
        MutableCentralConfigDescriptor mutableCentralConfigDescriptor = centralConfigService.getMutableDescriptor();
        MailServerDescriptor mailServer = mutableCentralConfigDescriptor.getMailServer();
        if (mailServer != null && StringUtils.isNotBlank(mailServer.getArtifactoryUrl())) {
            return mailServer.getArtifactoryUrl();
        }
        return null;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public boolean isCreateDefaultAdminAccountAllowed() {
        return true;
    }

    @Override
    public boolean isAolAdmin() {
        return false;
    }

    @Override
    public boolean isAolAdmin(UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean isAolAdmin(String username) {
        return false;
    }

    @Override
    public boolean isAol() {
        return false;
    }

    @Override
    @Nonnull
    public List<String> getUsersForBackupNotifications() {
        List<UserInfo> allUsers = ContextHelper.get().beanForType(UserGroupService.class).getAllUsers(true);
        List<String> adminEmails = Lists.newArrayList();
        for (UserInfo user : allUsers) {
            if (user.isAdmin()) {
                if (StringUtils.isNotBlank(user.getEmail())) {
                    adminEmails.add(user.getEmail());
                } else {
                    log.debug("User '{}' has no email address.", user.getUsername());
                }
            }
        }
        return adminEmails;
    }

    @Override
    public void validateTargetHasDifferentLicenseKeyHash(String targetLicenseHash, List<String> addons) {
        AddonsManager addonsManager = getAddonsManager();
        // Skip Trial license
        if (isTrial(addonsManager)) {
            log.debug("Source has trial license, skipping target instance license validation.");
            return;
        }
        if (StringUtils.isBlank(targetLicenseHash)) {
            if (addons == null || !addons.contains(AddonType.REPLICATION.getAddonName())) {
                throw new IllegalArgumentException(
                        "Replication between an open-source Artifactory instance is not supported.");
            }

            throw new IllegalArgumentException(
                    "Could not retrieve license key from remote target, user must have deploy permissions.");
        }
        if (addonsManager.getLicenseKeyHash().equals(targetLicenseHash)) {
            throw new IllegalArgumentException("Replication between same-license servers is not supported.");
        }
    }

    @Override
    public void validateMultiPushReplicationSupportedForTargetLicense(String targetLicenseKey,
            boolean isMultiPushConfigure, String targetUrl) {
        AddonsManager addonsManager = getAddonsManager();
        if (!addonsManager.isLicenseKeyHashHAType(targetLicenseKey) && isMultiPushConfigure) {
            log.info("Multi Push Replication is not supported for target :" + targetUrl);
            throw new IllegalArgumentException(
                    "Multi Push Replication is supported for targets with an enterprise license only");
        }
    }

    @Override
    public String getBuildNum() {
        VersionInfo versionInfo = centralConfigService.getVersionInfo();
        return format("%s rev %s", versionInfo.getVersion(), versionInfo.getRevision());
    }

    private boolean isTrial(AddonsManager addonsManager) {
        return addonsManager.isLicenseInstalled() && "Trial".equalsIgnoreCase(addonsManager.getLicenseDetails()[2]);
    }

    private CentralConfigService getCentralConfig() {
        return ArtifactoryApplication.get().getCentralConfig();
    }

    @Override
    public void addLayoutCopyLink(List<AbstractLink> links, RepoLayout layoutToCopy, String linkId, String linkTitle,
            LayoutListPanel layoutListPanel) {
    }

    @Override
    public AbstractLink getNewLayoutItemLink(String linkId, String linkTitle, LayoutListPanel layoutListPanel) {
        BaseTitledLink baseTitledLink = new BaseTitledLink(linkId, linkTitle);
        baseTitledLink.add(new CssClass("button-disabled"));
        baseTitledLink.add(new DisabledAddonBehavior(AddonType.LAYOUTS));
        baseTitledLink.add(new CssClass("green-button"));
        return baseTitledLink;
    }

    @Override
    public ITab getHttpRepoReplicationPanel(String tabTitle, HttpRepoDescriptor repoDescriptor,
            RemoteReplicationDescriptor replicationDescriptor, CreateUpdateAction action) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.REPLICATION);
    }

    @Override
    public ITab getLocalRepoReplicationPanel(String tabTitle, LocalRepoDescriptor entity,
            LocalReplicationDescriptor replicationDescriptor,
            MutableCentralConfigDescriptor mutableDescriptor, CreateUpdateAction action) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.REPLICATION);
    }

    @Override
    public MarkupContainer getLastReplicationStatusLabel(String id, RepoPath repoPath, boolean isCache) {
        return new PlaceHolder(id);
    }

    @Override
    public void createAndAddLocalRepoYumSection(Form<LocalRepoDescriptor> form, LocalRepoDescriptor descriptor, boolean isCreate) {
        WebMarkupContainer calculationBorder = new WebMarkupContainer("yumCalculationBorder");
        calculationBorder.setOutputMarkupId(true);
        calculationBorder.add(new TitledBorderBehavior("fieldset-border", "YUM Calculation"));
        calculationBorder.add(new DisabledAddonBehavior(AddonType.YUM));
        calculationBorder.add(new StyledCheckbox("calculateYumMetadata").setTitle("Auto-calculate YUM Metadata")
                .setEnabled(false));
        calculationBorder.add(new SchemaHelpBubble("calculateYumMetadata.help").setEnabled(false));
        TitledAjaxSubmitLink runCalculationButton = new TitledAjaxSubmitLink("calculateNow", "Calculate Now", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        };
        runCalculationButton.setEnabled(false);
        calculationBorder.add(runCalculationButton);
        calculationBorder.add(new TextField<Integer>("yumRootDepth").setEnabled(false));
        calculationBorder.add(new SchemaHelpBubble("yumRootDepth.help").setEnabled(false));
        calculationBorder.add(new TextArea("yumGroupFileNames").setEnabled(false));
        calculationBorder.add(new SchemaHelpBubble("yumGroupFileNames.help").setEnabled(false));
        calculationBorder.setEnabled(false);
        form.add(calculationBorder);
    }

    @Override
    public ITab getRpmInfoTab(String tabTitle, FileInfo fileInfo) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.YUM);
    }

    @Override
    public void createAndAddLocalRepoDebianSection(Form form, RepoDescriptor repoDescriptor) {
        WebMarkupContainer calculationBorder = new WebMarkupContainer("debianSupportSection");
        calculationBorder.setOutputMarkupId(true);
        calculationBorder.add(new TitledBorderBehavior("fieldset-border", "Debian"));
        calculationBorder.add(new DisabledAddonBehavior(AddonType.DEBIAN));
        calculationBorder.add(new StyledCheckbox("enableDebianSupport").setTitle("Enable Debian Support")
                .setEnabled(false));
        calculationBorder.add(new SchemaHelpBubble("enableDebianSupport.help").setEnabled(false));
        if (repoDescriptor instanceof LocalRepoDescriptor) {
            Component debianTrivialLayout = new StyledCheckbox("debianTrivialLayout").setTitle(
                    "Trivial Layout").setEnabled(false);
            calculationBorder.add(debianTrivialLayout);
            calculationBorder.add(new SchemaHelpBubble("debianTrivialLayout.help", "debianTrivialLayout"));
            TitledAjaxSubmitLink runCalculationButton = new TitledAjaxSubmitLink("recalculateIndex",
                    "Recalculate Index",
                    form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                }
            };
            runCalculationButton.setEnabled(false);
            calculationBorder.add(runCalculationButton).setEnabled(false);
            runCalculationButton.setEnabled(false);
        }
        form.add(calculationBorder);
    }

    @Override
    public WebMarkupContainer getGpgKeyStorePanel(String id) {
        return new WebMarkupContainer(id);
    }

    @Nullable
    public ITab getDebianInfoTab(String tabTitle, FileInfo fileInfo) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.DEBIAN);
    }

    @Override
    public AbstractTab getVirtualRepoConfigurationTab(String tabTitle, VirtualRepoDescriptor repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.P2);
    }

    @Override
    public List<P2Repository> verifyRemoteRepositories(MutableCentralConfigDescriptor currentDescriptor,
            VirtualRepoDescriptor virtualRepo, List<P2Repository> currentList,
            Map<String, List<String>> subCompositeUrls, MutableStatusHolder statusHolder) {
        statusHolder.error("Error: the P2 addon is required for this operation.",
                HttpStatus.SC_BAD_REQUEST, log);
        return null;
    }

    @Override
    public void createAndAddRemoteRepoConfigP2Section(Form form, RemoteRepoDescriptor descriptor) {
        WebMarkupContainer p2Border = new WebMarkupContainer("p2Border");
        p2Border.add(new TitledBorderBehavior("fieldset-border", "P2 Support"));
        p2Border.add(new WebMarkupContainer("p2OriginalUrl"));
        p2Border.add(new DisabledAddonBehavior(AddonType.P2));
        form.add(p2Border);
        p2Border.add(new StyledCheckbox("p2Support").setEnabled(false));
        p2Border.add(new SchemaHelpBubble("p2Support.help"));
    }

    private AddonsManager getAddonsManager() {
        return ContextHelper.get().beanForType(AddonsManager.class);
    }

    @Override
    public void createAndAddRepoConfigNuGetSection(Form form, RepoDescriptor repoDescriptor, boolean isCreate) {
        form.add(getVirtualRepoConfigurationSection("nuGetBorder", repoDescriptor, form, isCreate));
    }

    @Override
    public WebMarkupContainer getVirtualRepoConfigurationSection(String id, RepoDescriptor repoDescriptor, Form form,
            boolean isCreate) {
        WebMarkupContainer nuGetBorder = new WebMarkupContainer(id);
        nuGetBorder.add(new TitledBorderBehavior("fieldset-border", "NuGet"));
        nuGetBorder.add(new DisabledAddonBehavior(AddonType.NUGET));
        nuGetBorder.add(new StyledCheckbox("enableNuGetSupport").setTitle("Enable NuGet Support").setEnabled(false));
        nuGetBorder.add(new SchemaHelpBubble("enableNuGetSupport.help"));
        nuGetBorder.add(new TitledAjaxSubmitLink("reindexPackages", "ReIndex Packages", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        }.setEnabled(false));

        if (repoDescriptor instanceof RemoteRepoDescriptor) {
            NuGetConfiguration dummyConf = new NuGetConfiguration();
            nuGetBorder.add(new TextField<>("feedContextPath",
                    new PropertyModel<String>(dummyConf, "feedContextPath")).setEnabled(false));
            nuGetBorder.add(new TextField<>("downloadContextPath",
                    new PropertyModel<String>(dummyConf, "downloadContextPath")).setEnabled(false));
            nuGetBorder.add(new SchemaHelpBubble("feedContextPath.help",
                    new SchemaHelpModel(dummyConf, "feedContextPath")));
            nuGetBorder.add(new SchemaHelpBubble("downloadContextPath.help",
                    new SchemaHelpModel(dummyConf, "downloadContextPath")));
        }
        nuGetBorder.add(getNuGetUrlLabel(repoDescriptor));
        return nuGetBorder;
    }

    @Override
    public ITab getVirtualRepoConfigurationTab(String tabTitle, VirtualRepoDescriptor repoDescriptor) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.NUGET);
    }

    @Override
    public HttpRequestBase getRemoteRepoTestMethod(String repoUrl, HttpRepoDescriptor repo) {
        return new HttpHead(HttpUtils.encodeQuery(repoUrl));
    }

    @Override
    public ITab buildPackagesConfigTab(String id, RepoDescriptor repoDescriptor, Form form) {
        return new DisabledAddonTab(Model.of(id), AddonType.GEMS);
    }

    @Override
    public ITab getNuPkgInfoTab(String tabTitle, RepoPath nuPkgRepoPath) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.NUGET);
    }

    @Override
    public Label getNuGetUrlLabel(RepoDescriptor repoDescriptor) {
        Label label = new Label("nuGetRepoUrlLabel", "");
        label.setVisible(false);
        return label;
    }

    @Override
    public MenuNode getSamlAddonMenuNode(String nodeName) {
        return new DisabledAddonMenuNode(nodeName, AddonType.SSO);
    }

    @Override
    public ITab getExternalComponentInfoTab(RepoAwareActionableItem repoItem) {
        return new DisabledAddonTab(Model.of("Governance"), AddonType.BLACKDUCK);
    }

    @Override
    public ITab getBuildInfoTab(String title, Build build, boolean hasDeployOnLocal) {
        return new DisabledAddonTab(Model.of("Governance"), AddonType.BLACKDUCK);
    }

    @Override
    public boolean shouldShowLicensesAddonTab(ITab governanceTab, Build build) {
        return true;
    }

    @Override
    public boolean isEnableIntegration() {
        return false;
    }

    @Override
    public WebMarkupContainer buildPackagesConfigSection(String id, RepoDescriptor descriptor, Form form) {
        WebMarkupContainer gemsSection = new WebMarkupContainer(id);
        gemsSection.add(new TitledBorderBehavior("fieldset-border", "RubyGems"));
        gemsSection.add(new DisabledAddonBehavior(AddonType.GEMS));
        gemsSection.add(new StyledCheckbox("enableGemsSupport").setTitle("Enable RubyGems Support").setEnabled(false));
        gemsSection.add(new SchemaHelpBubble("enableGemsSupport.help"));
        gemsSection.add(new TitledAjaxSubmitLink("recalculateIndex", "Recalculate Index", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        }.setEnabled(false));

        Label label = new Label("gemsRepoUrlLabel", "");
        label.setVisible(false);
        gemsSection.add(label);

        return gemsSection;
    }

    @Override
    public WebMarkupContainer buildInfoSection(String id, RepoPath repoPath) {
        return (WebMarkupContainer) new WebMarkupContainer(id).add(new DisabledAddonBehavior(GEMS));
    }

    @Override
    public WebMarkupContainer buildDistributionManagementPanel(String id, RepoPath repoPath) {
        return new WebMarkupContainer(id);
    }

    @Override
    public void createAndAddRepoConfigNpmSection(Form form, RepoDescriptor repoDescriptor, boolean isCreate) {
        WebMarkupContainer npmSection = new WebMarkupContainer("npmSupportSection");
        npmSection.add(new TitledBorderBehavior("fieldset-border", "Npm"));
        npmSection.add(new DisabledAddonBehavior(AddonType.NPM));
        npmSection.add(new StyledCheckbox("enableNpmSupport").setTitle("Enable Npm Support").setEnabled(false));
        npmSection.add(new SchemaHelpBubble("enableNpmSupport.help"));
        npmSection.add(new TitledAjaxSubmitLink("recalculateIndex", "Recalculate Index", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        }.setEnabled(false));
        Label label = new Label("npmRepoUrlLabel", "");
        label.setVisible(false);
        npmSection.add(label);
        form.add(npmSection);
    }

    @Override
    public ITab getNpmInfoTab(String tabTitle, FileInfo fileInfo) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.NPM);
    }

    @Override
    public void createAndAddRepoConfigDockerSection(Form form, RepoDescriptor repoDescriptor, boolean isCreate) {
        WebMarkupContainer dockerSection = new WebMarkupContainer("dockerSupportSection");
        dockerSection.add(new TitledBorderBehavior("fieldset-border", "Docker"));
        dockerSection.add(new DisabledAddonBehavior(AddonType.DOCKER));
        dockerSection.add(new StyledCheckbox("enableDockerSupport").setTitle("Enable Docker Support").setEnabled(false));
        dockerSection.add(new SchemaHelpBubble("enableDockerSupport.help"));
        Label label = new Label("dockerRepoUrlLabel", "");
        label.setVisible(false);
        dockerSection.add(label);

        if (repoDescriptor instanceof LocalRepoDescriptor) {
            final RadioGroup dockerApiVersion = new RadioGroup("dockerApiVersion");
            dockerApiVersion.add(new Radio<>("v1", Model.of(DockerApiVersion.V1)));
            dockerApiVersion.add(new HelpBubble("v1.help", "Support Docker V1 API"));
            dockerApiVersion.add(new Radio<>("v2", Model.of(DockerApiVersion.V2)));
            dockerApiVersion.add(new HelpBubble("v2.help", "Support Docker V2 API"));
            dockerSection.add(dockerApiVersion);
        } else if (repoDescriptor instanceof RemoteRepoDescriptor) {
            dockerSection.add(new StyledCheckbox("dockerTokenAuthentication").setTitle("Enable Token Authentication").setEnabled(false));
            dockerSection.add(new SchemaHelpBubble("dockerTokenAuthentication.help"));
        }
        form.add(dockerSection);
    }

    @Override
    public ITab getDockerInfoTab(String tabTitle, FileInfo fileInfo) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.DOCKER);
    }

    @Override
    public ITab getDockerAncestryTab(String tabTitle, FileInfo fileInfo) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.DOCKER);
    }

    @Override
    public DeleteAction getDeleteAction(ItemInfo itemInfo) {
        return new DeleteAction();
    }

    @Override
    public String getFolderCssClass(RepoPath repoPath, LocalRepoDescriptor repo) {
        // Null means we fallback to default
        return null;
    }

    @Override
    public void createAndAddPypiConfigSection(Form form, RepoDescriptor repo, boolean isCreate) {
        WebMarkupContainer section = new WebMarkupContainer("pypiSupportSection");
        section.add(new TitledBorderBehavior("fieldset-border", "PyPI"));
        section.add(new DisabledAddonBehavior(AddonType.PYPI));
        section.add(new StyledCheckbox("enablePypiSupport").setTitle("Enable PyPI Support").setEnabled(false));
        section.add(new SchemaHelpBubble("enablePypiSupport.help"));
        if (repo instanceof LocalRepoDescriptor) {
            section.add(new TitledAjaxSubmitLink("recalculateIndex", "Recalculate Index", form) {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                }
            }.setEnabled(false));
        }
        section.add(new Label("pypiRepoUrlLabel", "").setVisible(false));
        form.add(section);
    }

    @Override
    public ITab createPypiPackageInfoTab(String title, RepoPath packagePath) {
        return new DisabledAddonTab(Model.of(title), AddonType.PYPI);
    }

    @Override
    public boolean isPypiFile(FileInfo fileInfo) {
        return false;
    }

    @Override
    public void createAndAddRepoConfigBowerSection(Form form, RepoDescriptor repoDescriptor, boolean isCreate) {
        WebMarkupContainer bowerSection = new WebMarkupContainer("bowerSupportSection");
        bowerSection.add(new TitledBorderBehavior("fieldset-border", "Bower"));
        bowerSection.add(new DisabledAddonBehavior(AddonType.BOWER));
        bowerSection.add(new StyledCheckbox("enableBowerSupport").setTitle("Enable Bower Support").setEnabled(false));
        bowerSection.add(new SchemaHelpBubble("enableBowerSupport.help"));
        bowerSection.add(new TitledAjaxSubmitLink("reindexPackages", "ReIndex Packages", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        }.setEnabled(false));

        Label label = new Label("bowerRepoUrlLabel", "");
        label.setVisible(false);

        if (repoDescriptor instanceof RemoteRepoDescriptor) {
            BowerConfiguration bowerConf = new BowerConfiguration();
            bowerSection.add(new TextField<>("bowerRegistryUrl",
                    new PropertyModel<String>(bowerConf, "bowerRegistryUrl")).setEnabled(false));
            bowerSection.add(new SchemaHelpBubble("bowerRegistryUrl.help",
                    new SchemaHelpModel(bowerConf, "bowerRegistryUrl")));
        }

        bowerSection.add(label);
        form.add(bowerSection);
    }

    @Override
    public ITab getBowerInfoTab(String tabTitle, FileInfo fileInfo) {
        return new DisabledAddonTab(Model.of(tabTitle), AddonType.BOWER);
    }

    @Override
    public boolean isBowerFile(String filePath) {
        return false;
    }

    @Override
    public void createAndAddVcsConfigSection(Form form, RemoteRepoDescriptor repoDescriptor, boolean isCreate) {
        WebMarkupContainer vcsSection = new WebMarkupContainer("vcsSupportSection");
        vcsSection.add(new TitledBorderBehavior("fieldset-border", "Vcs"));
        vcsSection.add(new DisabledAddonBehavior(AddonType.VCS));
        vcsSection.add(new StyledCheckbox("enableVcsSupport").setTitle("Enable Vcs Support").setEnabled(false));
        vcsSection.add(new DisabledAddonHelpBubble("enableVcsSupport.help", VCS));

        DropDownChoice providerDropdown = new DropDownChoice("provider", Model.of(), Collections.emptyList());
        providerDropdown.setEnabled(false);
        providerDropdown.add(new DisabledAddonBehavior(AddonType.VCS));
        vcsSection.add(providerDropdown);
        vcsSection.add(new DisabledAddonHelpBubble("provider.help", VCS));

        WebMarkupContainer downloadUrlField = new WebMarkupContainer("downloadUrlField");
        downloadUrlField.setVisible(false);
        downloadUrlField.add(new TextField("downloadUrl").setEnabled(false).setVisible(false));
        downloadUrlField.add(new DisabledAddonHelpBubble("downloadUrl.help", VCS).setVisible(false));
        vcsSection.add(downloadUrlField);

        form.add(vcsSection);
    }

    @Override
    public void createAndAddRepoConfigVagrantSection(Form<LocalRepoDescriptor> form, LocalRepoDescriptor descriptor) {
        WebMarkupContainer vagrantSection = new WebMarkupContainer("vagrantSupportSection");
        vagrantSection.add(new TitledBorderBehavior("fieldset-border", "Vagrant"));
        vagrantSection.add(new DisabledAddonBehavior(AddonType.VAGRANT));
        vagrantSection.add(new StyledCheckbox("enableVagrantSupport").setTitle("Enable Vagrant Support").setEnabled(false));
        vagrantSection.add(new SchemaHelpBubble("enableVagrantSupport.help"));
        Label label = new Label("vagrantRepoUrlLabel", "");
        label.setVisible(false);
        vagrantSection.add(label);
        form.add(vagrantSection);

    }

    @Override
    public void createAndAddRepoConfigGitLfsSection(Form<LocalRepoDescriptor> form, RepoDescriptor descriptor) {
        WebMarkupContainer gitLfsSection = new WebMarkupContainer("gitLfsSupportSection");
        gitLfsSection.add(new TitledBorderBehavior("fieldset-border", "Git LFS"));
        gitLfsSection.add(new DisabledAddonBehavior(AddonType.GITLFS));
        gitLfsSection.add(
                new StyledCheckbox("enableGitLfsSupport").setTitle("Enable Git LFS Support").setEnabled(false));
        gitLfsSection.add(new SchemaHelpBubble("enableGitLfsSupport.help"));
        Label label = new Label("gitLfsRepoUrlLabel", "");
        label.setVisible(false);
        gitLfsSection.add(label);
        form.add(gitLfsSection);
    }

    @Override
    public ItemCssClass getFileCssClass(RepoPath path) {
        //null falls back to default in FileActionableItem
        return null;
    }

    @Override
    public void executeAdditiveRealmPlugins() {

    }

    private static class UpdateNewsFromCache extends AbstractAjaxTimerBehavior {

        @SpringBean
        private VersionInfoService versionInfoService;

        public UpdateNewsFromCache() {
            super(Duration.seconds(5));
            Injector.get().inject(this);
        }

        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator() {
            return new NoAjaxIndicatorDecorator();
        }

        @Override
        protected void onTimer(AjaxRequestTarget target) {
            stop(); // try only once
            VersionHolder latestVersion = versionInfoService.getLatestVersionFromCache(true);
            if (!VersionInfoService.SERVICE_UNAVAILABLE.equals(latestVersion.getVersion())) {
                getComponent().setDefaultModelObject(buildLatestVersionLabel(latestVersion));
                target.add(getComponent());
            }
        }
    }

    private static class NopAction extends ItemAction {
        public NopAction() {
            super("");
        }

        @Override
        public void onAction(ItemEvent e) {
        }
    }

    private static class DisabledBuildsTab extends BaseTab {
        private final RepoAwareActionableItem item;

        public DisabledBuildsTab(RepoAwareActionableItem item) {
            super("Builds");
            this.item = item;
        }

        @Override
        public Panel getPanel(String panelId) {
            return new DisabledBuildsTabPanel(panelId, item);
        }

        @Override
        public void onNewTabItem(LoopItem item) {
            super.onNewTabItem(item);
            item.add(new AddonNeededBehavior(AddonType.BUILD));
        }
    }

    private static class DisabledPublishedTab extends BaseTab {
        public DisabledPublishedTab() {
            super("Published Modules");
        }

        @Override
        public Panel getPanel(String panelId) {
            return new DisabledModuleInfoTabPanel(panelId);
        }

        @Override
        public void onNewTabItem(LoopItem item) {
            super.onNewTabItem(item);
            item.add(new AddonNeededBehavior(AddonType.BUILD));
        }
    }

    private static class DisabledBuildDiffTab extends BaseTab {
        public DisabledBuildDiffTab() {
            super("Diff");
        }

        @Override
        public Panel getPanel(String panelId) {
            return new DisabledBuildDiffTabPanel(panelId);
        }

        @Override
        public void onNewTabItem(LoopItem item) {
            super.onNewTabItem(item);
            item.add(new AddonNeededBehavior(AddonType.BUILD));
        }
    }

    private static class DisabledLdapGroupListPanel extends LdapGroupListPanel {
        public DisabledLdapGroupListPanel(String id) {
            super(id);
            add(new CssClass("disabled-panel"));
            add(new AddonNeededBehavior(AddonType.LDAP).setPosition("above", "below"));
            disableAll(this);
        }
    }

    private static class DisabledBuildsTabPanel extends BaseBuildsTabPanel {

        /**
         * Main constructor
         *
         * @param id   ID to assign to the panel
         * @param item Selected repo item
         */
        public DisabledBuildsTabPanel(String id, RepoAwareActionableItem item) {
            super(id, item);
            add(new CssClass("disabled-panel"));

            disableAll(this);
        }

        @Override
        protected List<BuildRun> getArtifactBuilds() {
            return Lists.newArrayList();
        }

        @Override
        protected List<BuildRun> getDependencyBuilds() {
            return Lists.newArrayList();
        }

        @Override
        protected List<BuildTabActionableItem> getArtifactActionableItems(BuildRun run) {
            return Lists.newArrayList();
        }

        @Override
        protected List<BuildDependencyActionableItem> getDependencyActionableItems(BuildRun run) {
            return Lists.newArrayList();
        }
    }

    private static class DisabledPropertiesPanel extends PropertiesPanel {
        public DisabledPropertiesPanel(String id, String nestedPanelId) {
            super(id);
            add(new DisabledCollapsibleBehavior());
            add(new WebMarkupContainer(nestedPanelId));
        }

        @Override
        protected Component newToolbar(String id) {
            return new DisabledAddonHelpBubble(id, PROPERTIES);
        }
    }

    private static class DisabledSettingsProvisioningBorder extends BaseSettingsProvisioningBorder {

        protected DisabledSettingsProvisioningBorder(String id, Form form, TextArea<String> content,
                String saveToFileName) {
            super(id, form, content, saveToFileName);
            setEnabled(false);
        }

        @Override
        protected Component newToolbar(String id) {
            return new DisabledAddonHelpBubble(id, FILTERED_RESOURCES);
        }

        @Override
        protected List<? extends LocalRepoDescriptor> getDeployableRepoDescriptors() {
            return Lists.newArrayList();
        }

        @Override
        protected Component getDeploymentLink(String id, String title, Form form, TextArea<String> editableContent) {
            return new BaseTitledLink(id, title).setEnabled(false);
        }
    }
}
