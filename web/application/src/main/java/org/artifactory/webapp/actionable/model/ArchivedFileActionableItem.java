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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.artifactory.util.TreeNode;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.viewable.ViewableTabPanel;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.zipentry.ZipEntryGeneralTabPanel;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.List;
import java.util.Set;

/**
 * Represents a file or directory inside a zip.
 *
 * @author Yossi Shaul
 */
public class ArchivedFileActionableItem extends ArchivedItemActionableItem {

    public ArchivedFileActionableItem(RepoPath archivePath, TreeNode<ZipEntryInfo> node) {
        super(archivePath, node);
        Set<ItemAction> actions = getActions();
        FilteredResourcesWebAddon filteredAddon = getAddonsProvider().addonByType(FilteredResourcesWebAddon.class);
        actions.add(filteredAddon.getZipEntryDownloadAction());
    }

    @Override
    public String getDisplayName() {
        return node.getData().getName();
    }

    public String getPath() {
        return node.getData().getPath();
    }

    public ZipEntryInfo getZipEntry() {
        return node.getData();
    }

    @Override
    public void addTabs(List<ITab> tabs) {
        tabs.add(new AbstractTab(Model.of("General")) {
            @Override
            public Panel getPanel(String panelId) {
                return new ZipEntryGeneralTabPanel(panelId, getZipEntry(), ArchivedFileActionableItem.this);
            }
        });

        if (NamingUtils.isViewable(getPath()) || "class".equals(PathUtils.getExtension(getPath()))) {
            tabs.add(new AbstractTab(Model.of("View Source")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new ViewableTabPanel(panelId, ArchivedFileActionableItem.this);
                }
            });
        }
    }

    @Override
    public void filterActions(AuthorizationService authService) {

    }

    @Override
    public String getCssClass() {
        return ItemCssClass.getFileCssClass(getPath()).getCssClass();
    }
}