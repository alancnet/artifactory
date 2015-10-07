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
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.util.Tree;
import org.artifactory.util.TreeNode;
import org.artifactory.webapp.actionable.ActionableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A zip file actionable item which is a file actionable item with hierarchy behavior that allows browsing the internals
 * of the zip.
 *
 * @author Yossi Shaul
 */
public class ZipFileActionableItem extends FileActionableItem implements HierarchicActionableItem {
    private static final Logger log = LoggerFactory.getLogger(ZipFileActionableItem.class);

    private boolean compactAllowed;

    public ZipFileActionableItem(org.artifactory.fs.FileInfo fileInfo, boolean compactAllowed) {
        super(fileInfo);
        this.compactAllowed = compactAllowed;
    }

    @Override
    public List<ActionableItem> getChildren(AuthorizationService authService) {
        Tree<ZipEntryInfo> tree;
        try {
            tree = getRepoService().zipEntriesToTree(getFileInfo().getRepoPath());
        } catch (IOException e) {
            log.error("Failed to retrieve zip entries: " + e.getMessage());
            return Collections.emptyList();
        }

        TreeNode<ZipEntryInfo> root = tree.getRoot();
        if (!root.hasChildren()) {
            return Collections.emptyList();
        }

        List<ActionableItem> items = Lists.newArrayList();
        Collection<? extends TreeNode<ZipEntryInfo>> children = root.getChildren();
        for (TreeNode<ZipEntryInfo> childTreeNode : children) {
            if (childTreeNode.getData().isDirectory()) {
                items.add(new ArchivedFolderActionableItem(getRepoPath(), childTreeNode, isCompactAllowed()));
            } else {
                items.add(new ArchivedFileActionableItem(getRepoPath(), childTreeNode));
            }
        }
        return items;
    }

    @Override
    public boolean hasChildren(AuthorizationService authService) {
        // always assume the zip has children (we'll make sure it does later)
        return true;
    }

    @Override
    public boolean isCompactAllowed() {
        return compactAllowed;
    }

    @Override
    public void setCompactAllowed(boolean compactAllowed) {
        this.compactAllowed = compactAllowed;
    }
}
