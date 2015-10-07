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

package org.artifactory.webapp.wicket.page.browse.treebrowser;

import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.wicket.actionable.tree.ActionableItemTreeNode;
import org.artifactory.webapp.wicket.actionable.tree.DefaultTreeSelection;

import javax.annotation.Nonnull;
import java.util.Enumeration;

/**
 * Repository tree default selection helper implementation
 *
 * @author Noam Y. Tenne
 */
public class DefaultRepoTreeSelection implements DefaultTreeSelection {

    private RepoPath repoPath;

    /**
     * Main constructor
     *
     * @param repoPath Repo path to auto select. May be null if non should be selected
     */
    public DefaultRepoTreeSelection(RepoPath repoPath) {
        this.repoPath = repoPath;
    }

    @Override
    public String getDefaultSelectionTreePath() {
        if (repoPath == null) {
            return "";
        }

        return getTreePath(repoPath);
    }

    @Override
    @Nonnull
    public ActionableItemTreeNode getNodeAt(@Nonnull ActionableItemTreeNode parentNode, String path) {
        String firstPart = PathUtils.getFirstPathElement(path);
        if (firstPart.length() > 0) {
            Enumeration children = parentNode.children();
            while (children.hasMoreElements()) {
                ActionableItemTreeNode child = (ActionableItemTreeNode) children.nextElement();
                RepoAwareActionableItem childItem = (RepoAwareActionableItem) child.getUserObject();
                RepoPath childRepoPath = childItem.getRepoPath();
                String name = PathUtils.getFileName(getTreePath(childRepoPath));
                if (name.equals(firstPart)) {
                    //Handle compacted folders
                    String displayName = child.getUserObject().getDisplayName();
                    int from = path.indexOf(displayName) + displayName.length() + 1;
                    String newPath = from < path.length() ? path.substring(from) : "";
                    return getNodeAt(child, newPath);
                }
            }
        }
        return parentNode;
    }

    /**
     * Constructs and returns a tree path out of a repo path. Required when the repo path points to metadata.
     *
     * @param repoPath Repo path to construct as tree path
     * @return Tree path
     */
    private static String getTreePath(RepoPath repoPath) {
        StringBuilder builder = new StringBuilder(repoPath.getRepoKey()).append("/");
        String path = repoPath.getPath();
        if (NamingUtils.isMetadata(path)) {
            path = NamingUtils.getMetadataParentPath(path);
        }
        return builder.append(path).toString();
    }
}