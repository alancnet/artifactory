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

package org.artifactory.webapp.actionable.model;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BowerWebAddon;
import org.artifactory.addon.wicket.DockerWebAddon;
import org.artifactory.addon.wicket.GemsWebAddon;
import org.artifactory.addon.wicket.GitLfsWebAddon;
import org.artifactory.addon.wicket.NpmWebAddon;
import org.artifactory.addon.wicket.NuGetWebAddon;
import org.artifactory.addon.wicket.PypiWebAddon;
import org.artifactory.addon.wicket.WatchAddon;
import org.artifactory.addon.wicket.YumWebAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.ivy.IvyNaming;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.actionable.FileActionable;
import org.artifactory.webapp.actionable.RepoAwareActionableItemBase;
import org.artifactory.webapp.actionable.action.CopyAction;
import org.artifactory.webapp.actionable.action.DownloadAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.MoveAction;
import org.artifactory.webapp.actionable.action.ViewTextFileAction;
import org.artifactory.webapp.actionable.action.ZapAction;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.ivy.XmlViewTabPanel;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.jnlp.JnlpViewTabPanel;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.maven.PomViewTabPanel;
import org.artifactory.webapp.wicket.util.ItemCssClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.ivy.XmlViewTabPanel.XmlTypes.GENERAL_XML;
import static org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.ivy.XmlViewTabPanel.XmlTypes.IVY_XML;

/**
 * @author Yoav Landman
 */
public class FileActionableItem extends RepoAwareActionableItemBase implements FileActionable {
    private static final Logger log = LoggerFactory.getLogger(FileActionableItem.class);

    private final org.artifactory.fs.FileInfo fileInfo;
    private ItemAction downloadAction;
    private ItemAction viewAction;

    private ItemAction deleteAction;
    private ItemAction zapAction;
    private MoveAction moveAction;
    private CopyAction copyAction;
    private ItemAction watchAction;

    public FileActionableItem(org.artifactory.fs.FileInfo fileInfo) {
        super(fileInfo);
        this.fileInfo = fileInfo;
        Set<ItemAction> actions = getActions();
        viewAction = new ViewTextFileAction();
        actions.add(viewAction);
        downloadAction = new DownloadAction();
        actions.add(downloadAction);
        moveAction = new MoveAction();
        actions.add(moveAction);
        copyAction = new CopyAction();
        actions.add(copyAction);
        zapAction = new ZapAction();
        actions.add(zapAction);

        AddonsManager addonsManager = getAddonsProvider();
        DockerWebAddon dockerWebAddon = addonsManager.addonByType(DockerWebAddon.class);
        deleteAction = dockerWebAddon.getDeleteAction(fileInfo);
        actions.add(deleteAction);

        WatchAddon watchAddon = addonsManager.addonByType(WatchAddon.class);
        watchAction = watchAddon.getWatchAction(fileInfo.getRepoPath());
        actions.add(watchAction);
    }

    @Override
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public StatsInfo getStatsInfo() {
        return getRepoService().getStatsInfo(getRepoPath());
    }

    @Override
    public String getDisplayName() {
        return getFileInfo().getName();
    }

    @Override
    public String getCssClass() {
        ItemCssClass cssClass;
        String path = getRepoPath().getPath();
        cssClass = getAddonsProvider().addonByType(GitLfsWebAddon.class).getFileCssClass(getRepoPath());
        return cssClass != null ? cssClass.getCssClass() : ItemCssClass.getFileCssClass(path).getCssClass();
    }

    @Override
    public void addTabs(List<ITab> tabs) {
        super.addTabs(tabs);
        boolean showTabs = shouldShowTabs();
        if (showTabs) {
            return;
        }

        if (isPomFile()) {
            // add pom view panel
            tabs.add(new AbstractTab(Model.of("Pom View")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new PomViewTabPanel(panelId, FileActionableItem.this);
                }
            });
        }

        if (isXmlFile() && !isPomFile()) {
            //xml tab
            final XmlViewTabPanel.XmlTypes xmlType = isIvyFile() ? IVY_XML : GENERAL_XML;
            tabs.add(new AbstractTab(Model.of(xmlType.getTabTitle())) {

                @Override
                public Panel getPanel(String panelId) {
                    return new XmlViewTabPanel(panelId, FileActionableItem.this, xmlType);
                }
            });
        }

        if (isJnlpFile()) {
            tabs.add(new AbstractTab(Model.of("JNLP")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new JnlpViewTabPanel(panelId, FileActionableItem.this);
                }
            });
        }

        if (isRpmFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            YumWebAddon yumWebAddon = addonsProvider.addonByType(YumWebAddon.class);
            ITab rpmInfoTab = yumWebAddon.getRpmInfoTab("RPM Info", getFileInfo());
            tabs.add(rpmInfoTab);
        }

        if (getRepo().getType().equals(RepoType.NuGet) && isNuPkgFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            NuGetWebAddon nuGetWebAddon = addonsProvider.addonByType(NuGetWebAddon.class);
            try {
                ITab nuPkgInfoTab = nuGetWebAddon.getNuPkgInfoTab("NuPkg Info", getRepoPath());
                if (nuPkgInfoTab != null) {
                    tabs.add(nuPkgInfoTab);
                }
            } catch (Exception e) {
                log.error("Error occurred while processing NuPkg display info: " + e.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while processing NuPkg display info.", e);
                }
            }
        }

        addPypiPackageInfoTab(tabs);

        if (getRepo().getType().equals(RepoType.Gems) && isGemFile()) {
            tabs.add(new AbstractTab(Model.of("RubyGems")) {
                //transient otherwise [ERROR] (o.a.w.s.j.JavaSerializer:94) ... java.io.NotSerializableException ...
                final transient GemsWebAddon gemsWebAddon = getAddonsProvider().addonByType(GemsWebAddon.class);

                @Override
                public WebMarkupContainer getPanel(String panelId) {
                    return gemsWebAddon.buildInfoSection(panelId, getRepoPath());
                }
            });
        }

        if (getRepo().getType().equals(RepoType.Npm) && isNpmFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            NpmWebAddon npmWebAddon = addonsProvider.addonByType(NpmWebAddon.class);
            ITab npmInfoTab = npmWebAddon.getNpmInfoTab("Npm Info", getFileInfo());
            tabs.add(npmInfoTab);
        }

        if (getRepo().getType().equals(RepoType.Bower) && isBowerFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            BowerWebAddon bowerWebAddon = addonsProvider.addonByType(BowerWebAddon.class);
            ITab bowerInfoTab = bowerWebAddon.getBowerInfoTab("Bower Info", getFileInfo());
            tabs.add(bowerInfoTab);
        }

        if (getRepo().getType().equals(RepoType.Docker) && isDockerJsonFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            DockerWebAddon dockerWebAddon = addonsProvider.addonByType(DockerWebAddon.class);
            ITab dockerInfoTab = dockerWebAddon.getDockerInfoTab("Docker Info", getFileInfo());
            tabs.add(dockerInfoTab);
        }

        if (getRepo().getType().equals(RepoType.Docker) && isDockerAncestryFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            DockerWebAddon dockerWebAddon = addonsProvider.addonByType(DockerWebAddon.class);
            ITab dockerAncestryTab = dockerWebAddon.getDockerAncestryTab("Docker Ancestry", getFileInfo());
            tabs.add(dockerAncestryTab);
        }
    }

    private void addPypiPackageInfoTab(List<ITab> tabs) {
        if (getRepo().getType().equals(RepoType.Pypi) && isPypiFile()) {
            AddonsManager addonsProvider = getAddonsProvider();
            PypiWebAddon pypiWebAddon = addonsProvider.addonByType(PypiWebAddon.class);
            try {
                ITab pypiPackageInfoTab = pypiWebAddon.createPypiPackageInfoTab("PyPI Info", getRepoPath());
                if (pypiPackageInfoTab != null) {
                    tabs.add(pypiPackageInfoTab);
                }
            } catch (Exception e) {
                log.error("Error occurred while processing PyPI info tab: " + e.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred while processing PyPI info.", e);
                }
            }
        }
    }

    @Override
    public void filterActions(AuthorizationService authService) {
        RepoPath repoPath = getFileInfo().getRepoPath();
        boolean canAdmin = authService.canManage(repoPath);
        boolean canDelete = authService.canDelete(repoPath);
        boolean canRead = authService.canRead(repoPath);
        if (!canDelete) {
            deleteAction.setEnabled(false);
        }
        if (!canAdmin) {
            zapAction.setEnabled(false);
        } else if (!getRepo().isCache()) {
            zapAction.setEnabled(false);
        }

        if (!NamingUtils.isViewable(getFileInfo().getName())) {
            viewAction.setEnabled(false);
        }

        if (isJnlpFile()) {
            downloadAction.setEnabled(false);
        }

        if (!canDelete || NamingUtils.isSystem(repoPath.getPath()) || !authService.canDeployToLocalRepository()) {
            moveAction.setEnabled(false);
        }

        if (!canRead || NamingUtils.isSystem(repoPath.getPath()) || !authService.canDeployToLocalRepository()) {
            copyAction.setEnabled(false);
        }

        if (!canRead || authService.isAnonymous()) {
            watchAction.setEnabled(false);
        }
    }

    private boolean isXmlFile() {
        return NamingUtils.isXml(getFileInfo().getName());
    }

    private boolean isIvyFile() {
        return IvyNaming.isIvyFileName(getFileInfo().getName());
    }

    private boolean isPomFile() {
        return MavenNaming.isPom(getFileInfo().getName());
    }

    private boolean isJnlpFile() {
        MimeType mimeType = NamingUtils.getMimeType(getFileInfo().getName());
        return "application/x-java-jnlp-file".equalsIgnoreCase(mimeType.getType());
    }

    private boolean isRpmFile() {
        return NamingUtils.isRpmFile(getFileInfo().getName());
    }

    private boolean isNuPkgFile() {
        return NamingUtils.isNuPkgFile(getFileInfo().getName());
    }

    private boolean isGemFile() {
        return NamingUtils.isGemFile(getFileInfo().getName());
    }

    private boolean isNpmFile() {
        return NamingUtils.isNpmFile(getFileInfo().getName());
    }

    private boolean isBowerFile() {
        return getAddonsProvider().addonByType(BowerWebAddon.class).isBowerFile(getFileInfo().getName());
    }

    private boolean isPypiFile() {
        AddonsManager addonsProvider = getAddonsProvider();
        PypiWebAddon pypiWebAddon = addonsProvider.addonByType(PypiWebAddon.class);
        return pypiWebAddon.isPypiFile(getFileInfo());
    }

    private boolean isDockerJsonFile() {
        return "json.json".equals(getFileInfo().getName());
    }

    private boolean isDockerAncestryFile() {
        return "ancestry.json".equals(getFileInfo().getName());
    }

    private boolean shouldShowTabs() {
        //Hack - dont display anything for checksums or metadata
        String name = getDisplayName();
        return NamingUtils.isChecksum(name) || NamingUtils.isMetadata(name);
    }
}