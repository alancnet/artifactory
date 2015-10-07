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

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.TreeNode;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.zipentry.ZipEntryPanel;
import org.artifactory.webapp.wicket.util.ItemCssClass;

import java.util.Collection;
import java.util.List;

/**
 * Represents a file or directory inside a zip.
 *
 * @author Yossi Shaul
 */
public class ArchivedFolderActionableItem extends ArchivedItemActionableItem implements HierarchicActionableItem {
    private boolean compactAllowed;
    private String displayName;
    private boolean compacted;

    public ArchivedFolderActionableItem(RepoPath archivePath, TreeNode<ZipEntryInfo> node, boolean compact) {
        super(archivePath, node);
        this.displayName = node.getData().getName();
        this.compactAllowed = compact;
        if (compact) {
            compact();
        }
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public ZipEntryInfo getZipEntry() {
        return node.getData();
    }

    @Override
    public void addTabs(List<ITab> tabs) {
        tabs.add(new AbstractTab(Model.of("General")) {
            @Override
            public Panel getPanel(String panelId) {
                return new ZipEntryPanel(panelId, getZipEntry());
            }
        });
    }

    @Override
    public void filterActions(AuthorizationService authService) {

    }

    @Override
    public List<ActionableItem> getChildren(AuthorizationService authService) {
        List<ActionableItem> items = Lists.newArrayList();
        Collection<? extends TreeNode<ZipEntryInfo>> children = node.getChildren();
        for (TreeNode<ZipEntryInfo> file : children) {
            if (file.getData().isDirectory()) {
                items.add(new ArchivedFolderActionableItem(getArchiveRepoPath(), file, compactAllowed));
            } else {
                items.add(new ArchivedFileActionableItem(getArchiveRepoPath(), file));
            }
        }
        return items;
    }

    @Override
    public boolean hasChildren(AuthorizationService authService) {
        return node.hasChildren();
    }

    @Override
    public boolean isCompactAllowed() {
        return compactAllowed;
    }

    @Override
    public void setCompactAllowed(boolean compactAllowed) {
        this.compactAllowed = compactAllowed;
    }

    private void compact() {
        StringBuilder compactedName = new StringBuilder(displayName);
        TreeNode<ZipEntryInfo> next = getNextCompactedNode(node);
        while (next != null) {
            compacted = true;
            node = next;
            compactedName.append('/').append(next.getData().getName());
            next = getNextCompactedNode(next);
        }
        displayName = compactedName.toString();
    }

    private TreeNode<ZipEntryInfo> getNextCompactedNode(TreeNode<ZipEntryInfo> next) {
        Collection<? extends TreeNode<ZipEntryInfo>> children = next.getChildren();

        // only compact folders with exactly one child
        if (children == null || children.size() != 1) {
            return null;
        }

        // the only child must be a folder
        TreeNode<ZipEntryInfo> firstChild = children.iterator().next();
        if (!firstChild.getData().isDirectory()) {
            return null;
        }

        return firstChild;
    }

    @Override
    public String getCssClass() {
        return compacted ? ItemCssClass.folderCompact.getCssClass() : ItemCssClass.folder.getCssClass();
    }
}