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

package org.artifactory.webapp.wicket.actionable.tree;

import javax.annotation.Nonnull;

/**
 * Default tree selection helper interface
 *
 * @author Noam Y. Tenne
 */
public interface DefaultTreeSelection {

    /**
     * Returns the path to auto-select in the tree
     *
     * @return Path to auto-select. May be null
     */
    String getDefaultSelectionTreePath();

    /**
     * Returns the deepest node matching the given path. For example if the parent looks like 'parent/child/1' and we
     * ask for 'child/1/2/3' the returned node will be child/1.
     *
     * @param parentNode The parent node of the path.
     * @param path       The path relative to the parent node we are looking for.
     * @return The deepest node under the parent node for the given path. If no node under the parent matches part of
     *         the path, the parent path is returned.
     */
    @Nonnull
    ActionableItemTreeNode getNodeAt(ActionableItemTreeNode parentNode, String path);
}