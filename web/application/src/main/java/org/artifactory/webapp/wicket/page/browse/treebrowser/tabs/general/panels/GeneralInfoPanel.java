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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BlackDuckWebAddon;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.addon.wicket.LicensesWebAddon;
import org.artifactory.addon.wicket.ReplicationWebAddon;
import org.artifactory.addon.wicket.WatchAddon;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.wicket.ajax.AjaxLazyLoadSpanPanel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.util.NumberFormatter;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.CannonicalEnabledActionableFolder;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.model.FolderActionableItem;
import org.artifactory.webapp.actionable.model.LocalRepoActionableItem;
import org.artifactory.webapp.servlet.RequestUtils;
import org.artifactory.webapp.wicket.page.browse.treebrowser.BrowseRepoPage;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.stats.StatsTabPanel;

/**
 * Displays general item information. Placed inside the general info panel.
 *
 * @author Yossi Shaul
 */
public class GeneralInfoPanel extends Panel {
    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private BintrayService bintrayService;

    public GeneralInfoPanel(String id) {
        super(id);
    }

    public GeneralInfoPanel init(RepoAwareActionableItem repoItem) {
        final boolean itemIsRepo = repoItem instanceof LocalRepoActionableItem;
        LocalRepoDescriptor repoDescriptor = repoItem.getRepo();
        final boolean isCache = repoDescriptor.isCache();
        RemoteRepoDescriptor remoteRepo = null;
        if (isCache) {
            remoteRepo = ((LocalCacheRepoDescriptor) repoDescriptor).getRemoteRepo();
        }

        FieldSetBorder infoBorder = new FieldSetBorder("infoBorder");
        add(infoBorder);

        LabeledValue nameLabel = new LabeledValue("name", "Name: ");
        infoBorder.add(nameLabel);

        String itemDisplayName = repoItem.getDisplayName();

        String pathUrl = BrowseRepoPage.getWicketDependableRepoPathUrl(repoItem);
        if (StringUtils.isBlank(pathUrl)) {
            pathUrl = "";
        }
        ExternalLink treeUrl = new ExternalLink("nameLink", pathUrl, itemDisplayName);
        infoBorder.add(treeUrl);
        infoBorder.add(new HelpBubble("nameLink.help",
                "Copy this link to navigate directly to this item in the tree browser."));

        LabeledValue descriptionLabel = new LabeledValue("description", "Description: ");
        descriptionLabel.setEscapeValue(false);
        String description = null;
        if (itemIsRepo) {
            if (isCache) {
                description = remoteRepo.getDescription();
            } else {
                description = repoDescriptor.getDescription();
            }
            if (description != null) {
                descriptionLabel.setValue(description.replace("\n", "<br/>"));
            }
        }
        descriptionLabel.setVisible(!StringUtils.isEmpty(description));
        infoBorder.add(descriptionLabel);

        ItemInfo itemInfo = repoItem.getItemInfo();

        LabeledValue deployedByLabel = new LabeledValue("deployed-by", "Deployed by: ", itemInfo.getModifiedBy()) {
            @Override
            public boolean isVisible() {
                return !itemIsRepo;
            }
        };
        infoBorder.add(deployedByLabel);

        //Add markup container in case we need to set the remote repo url
        WebMarkupContainer urlLabelContainer = new WebMarkupContainer("urlLabel");
        WebMarkupContainer urlContainer = new WebMarkupContainer("url");
        infoBorder.add(urlLabelContainer);
        infoBorder.add(urlContainer);

        if (isCache) {
            urlLabelContainer.replaceWith(new Label("urlLabel", "Remote URL: "));
            String remoteRepoUrl = remoteRepo.getUrl();
            if ((remoteRepoUrl != null) && (!StringUtils.endsWith(remoteRepoUrl, "/"))) {
                remoteRepoUrl += "/";
                if (repoItem instanceof FolderActionableItem) {
                    remoteRepoUrl += ((FolderActionableItem) repoItem).getCanonicalPath().getPath();
                } else {
                    remoteRepoUrl += repoItem.getRepoPath().getPath();
                }
            }
            ExternalLink externalLink = new ExternalLink("url", remoteRepoUrl, remoteRepoUrl);
            urlContainer.replaceWith(externalLink);
        }

        addOnlineStatusPanel(itemIsRepo, isCache, remoteRepo, infoBorder);

        final boolean repoIsBlackedOut = repoDescriptor.isBlackedOut();
        LabeledValue blackListedLabel = new LabeledValue("blackListed", "This repository is black-listed!") {
            @Override
            public boolean isVisible() {
                return repoIsBlackedOut;
            }
        };
        infoBorder.add(blackListedLabel);

        addArtifactCount(repoItem, infoBorder);

        addWatcherInfo(repoItem, infoBorder);

        final RepoPath path;
        if (repoItem instanceof FolderActionableItem) {
            path = ((FolderActionableItem) repoItem).getCanonicalPath();
        } else {
            path = repoItem.getRepoPath();
        }
        LabeledValue repoPath = new LabeledValue("repoPath", "Repository Path: ");
        infoBorder.add(repoPath);

        String pathLink = RequestUtils.getWicketServletContextUrl();
        if (!pathLink.endsWith("/")) {
            pathLink += "/";
        }
        pathLink += ArtifactoryRequest.SIMPLE_BROWSING_PATH + "/" + repoItem.getRepoPath().getRepoKey() + "/";
        if (repoItem instanceof CannonicalEnabledActionableFolder) {
            pathLink += ((CannonicalEnabledActionableFolder) repoItem).getCanonicalPath().getPath();
        } else {
            pathLink += PathUtils.getParent(repoItem.getRepoPath().getPath());
        }
        ExternalLink repoPathUrl = new ExternalLink("repoPathLink", pathLink, path + "");
        infoBorder.add(repoPathUrl);
        infoBorder.add(new HelpBubble("repoPathLink.help",
                "Copy this link to navigate directly to this item in the simple browser."));

        addItemInfoLabels(infoBorder, itemInfo);

        infoBorder.add(getLicenseInfo(repoItem));

        addLocalLayoutInfo(infoBorder, repoDescriptor, itemIsRepo);
        addRemoteLayoutInfo(infoBorder, remoteRepo, itemIsRepo);
        addLastReplicationInfo(infoBorder, path, isCache);

        addFilteredResourceCheckbox(infoBorder, itemInfo);

        infoBorder.add(new StatsTabPanel("statistics", itemInfo));

        addBintrayInfoPanel(infoBorder, itemInfo);

        return this;
    }

    private void addOnlineStatusPanel(boolean itemIsRepo, boolean cache, final RemoteRepoDescriptor remoteRepo,
            FieldSetBorder infoBorder) {
        final boolean isCacheRepo = itemIsRepo && cache;
        if (isCacheRepo) {
            infoBorder.add(new OnlineStatusPanel("onlineStatusPanel", remoteRepo));
        } else {
            infoBorder.add(new WebMarkupContainer("onlineStatusPanel"));
        }
    }

    private void addArtifactCount(final RepoAwareActionableItem repoItem, final FieldSetBorder infoBorder) {
        if (!repoItem.getItemInfo().isFolder()) {
            infoBorder.add(new WebMarkupContainer("artifactCountLabel"));
            infoBorder.add(new WebMarkupContainer("artifactCountValue"));
            WebMarkupContainer linkContainer = new WebMarkupContainer("link");
            linkContainer.setVisible(false);
            infoBorder.add(linkContainer);
        } else {
            infoBorder.add(new Label("artifactCountLabel", "Artifact Count: "));
            final WebMarkupContainer container = new WebMarkupContainer("artifactCountValue");
            infoBorder.add(container);
            AjaxLink<String> link = new AjaxLink<String>("link") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    setVisible(false);
                    container.replaceWith(new ArtifactCountLazySpanPanel("artifactCountValue", repoItem.getRepoPath()));
                    target.add(infoBorder);
                }
            };
            infoBorder.add(link);
        }
    }

    private static class ArtifactCountLazySpanPanel extends AjaxLazyLoadSpanPanel {
        @SpringBean
        private RepositoryService repositoryService;

        private final RepoPath repoPath;

        public ArtifactCountLazySpanPanel(final String id, RepoPath repoPath) {
            super(id);
            this.repoPath = repoPath;
        }

        @Override
        public Component getLazyLoadComponent(String markupId) {
            long count = repositoryService.getArtifactCount(repoPath);
            return new Label(markupId, NumberFormatter.formatLong(count));
        }
    }

    private void addWatcherInfo(RepoAwareActionableItem repoItem, FieldSetBorder infoBorder) {
        ItemInfo itemInfo = repoItem.getItemInfo();
        WatchAddon watchAddon = addonsManager.addonByType(WatchAddon.class);
        RepoPath selectedPath;

        if ((itemInfo.isFolder()) && (repoItem instanceof FolderActionableItem)) {
            selectedPath = ((FolderActionableItem) repoItem).getCanonicalPath();
        } else {
            selectedPath = itemInfo.getRepoPath();
        }

        infoBorder.add(watchAddon.getWatchingSinceLabel("watchingSince", selectedPath));
        infoBorder.add(watchAddon.getDirectlyWatchedPathPanel("watchedPath", selectedPath));
    }

    private Component getLicenseInfo(RepoAwareActionableItem actionableItem) {
        if (!actionableItem.getItemInfo().isFolder()) {
            BlackDuckWebAddon blackDuckWebAddon = addonsManager.addonByType(BlackDuckWebAddon.class);
            boolean enableIntegration = blackDuckWebAddon.isEnableIntegration();
            if (!blackDuckWebAddon.isDefault()) {
                if (enableIntegration) {
                    return blackDuckWebAddon.getBlackDuckLicenseGeneralInfoPanel(actionableItem);
                }
            }
            LicensesWebAddon licensesWebAddon = addonsManager.addonByType(LicensesWebAddon.class);
            return licensesWebAddon.getLicenseGeneralInfoPanel(actionableItem);
        } else {
            return new WebMarkupContainer("licensesPanel").setVisible(false);
        }
    }

    private void addItemInfoLabels(FieldSetBorder infoBorder, ItemInfo itemInfo) {
        LabeledValue sizeLabel = new LabeledValue("size", "Size: ");
        infoBorder.add(sizeLabel);

        LabeledValue lastModified = new LabeledValue("lastModified", "Last Modified: ");
        infoBorder.add(lastModified);
        HelpBubble lastModifiedHelp = new HelpBubble("lastModified.help",
                "The time this artifact's file was modified. \nWill be identical to the artifact's 'Created' date when not available \n(for example, when deploying without the 'X-Artifactory-Last-Modified' request header).");
        infoBorder.add(lastModifiedHelp);

        String created = centralConfigService.format(itemInfo.getCreated())+ " " + DurationFormatUtils.formatDuration(System.currentTimeMillis()-itemInfo.getCreated(),"(d'd' H'h' m'm' s's' ago)") ;
        LabeledValue createdLabel = new LabeledValue("created", "Created: ", created);
        infoBorder.add(createdLabel);

        infoBorder.add(new HelpBubble("created.help",
                "The time this artifact was deployed to or cached in Artifactory."));

        LabeledValue moduleId = new LabeledValue("moduleId", "Module ID: ");
        infoBorder.add(moduleId);

        // disable/enable and set info according to the node type
        if (itemInfo.isFolder()) {
            lastModified.setVisible(false);
            lastModifiedHelp.setVisible(false);
            sizeLabel.setVisible(false);
            moduleId.setVisible(false);
        } else {
            FileInfo file = (FileInfo) itemInfo;

            ModuleInfo moduleInfo = repositoryService.getItemModuleInfo(file.getRepoPath());

            long size = file.getSize();
            //If we are looking at a cached item, check the expiry from the remote repository
            String ageStr = centralConfigService.format(itemInfo.getLastModified()) + " " + DurationFormatUtils.formatDuration(file.getAge(), "(d'd' H'h' m'm' s's' ago)");
            lastModified.setValue(ageStr);
            sizeLabel.setValue(StorageUnit.toReadableString(size));
            if (moduleInfo.isValid()) {
                moduleId.setValue(moduleInfo.getPrettyModuleId());
            } else {
                moduleId.setValue("N/A");
            }
        }
    }

    private void addLocalLayoutInfo(FieldSetBorder infoBorder, LocalRepoDescriptor repoDescriptor, boolean itemIsRepo) {
        RepoLayout repoLayout = repoDescriptor.getRepoLayout();
        String name = "";
        if (repoLayout != null) {
            name = repoLayout.getName();
        }
        LabeledValue localLayoutLabel = new LabeledValue("localLayout", "Repository Layout: ", name);
        localLayoutLabel.setVisible(itemIsRepo);
        infoBorder.add(localLayoutLabel);
    }

    private void addRemoteLayoutInfo(FieldSetBorder infoBorder, RemoteRepoDescriptor remoteRepo, boolean itemIsRepo) {
        String componentId = "remoteLayout";
        if (!itemIsRepo || remoteRepo == null || remoteRepo.getRemoteRepoLayout() == null) {
            infoBorder.add(new WebMarkupContainer(componentId));
        } else {
            infoBorder.add(new LabeledValue(componentId, "Remote Repository Layout: ",
                    remoteRepo.getRemoteRepoLayout().getName()));
        }
    }

    private void addLastReplicationInfo(FieldSetBorder infoBorder, RepoPath repoPath, boolean isCache) {
        ReplicationWebAddon replicationWebAddon = addonsManager.addonByType(ReplicationWebAddon.class);
        infoBorder.add(replicationWebAddon.getLastReplicationStatusLabel("lastReplication", repoPath, isCache));
    }

    private void addFilteredResourceCheckbox(FieldSetBorder infoBorder, ItemInfo itemInfo) {
        WebMarkupContainer filteredResourceContainer = new WebMarkupContainer("filteredResourceContainer");
        filteredResourceContainer.setVisible(false);
        infoBorder.add(filteredResourceContainer);

        WebMarkupContainer filteredResourceCheckbox = new WebMarkupContainer("filteredResourceCheckbox");
        filteredResourceContainer.add(filteredResourceCheckbox);

        final WebMarkupContainer filteredResourceHelpBubble = new WebMarkupContainer("filteredResource.help");
        filteredResourceContainer.add(filteredResourceHelpBubble);

        if (!itemInfo.isFolder() && authorizationService.canAnnotate(itemInfo.getRepoPath())) {
            FilteredResourcesWebAddon filteredResourcesWebAddon =
                    addonsManager.addonByType(FilteredResourcesWebAddon.class);
            filteredResourceCheckbox.replaceWith(
                    filteredResourcesWebAddon.getFilteredResourceCheckbox("filteredResourceCheckbox", itemInfo));
            filteredResourceHelpBubble
                    .replaceWith(new HelpBubble("filteredResource.help", getString("filteredResource.help")));
            filteredResourceContainer.setVisible(true);
        }
    }

    private void addBintrayInfoPanel(FieldSetBorder infoBorder, final ItemInfo itemInfo) {
        ModuleInfo moduleInfo = repositoryService.getItemModuleInfo(itemInfo.getRepoPath());
        boolean offlineMode = centralConfigService.getDescriptor().isOfflineMode();
        if (itemInfo.isFolder() || offlineMode || moduleInfo.isIntegration()) {
            infoBorder.add(new WebMarkupContainer("bintrayDynamicInfoPanel"));
            infoBorder.add(new CssClass("bintrayDynamicPanelHidden"));
        } else {
            infoBorder.add(new BintrayDynamicInfoPanel("bintrayDynamicInfoPanel", itemInfo));
        }
    }
}