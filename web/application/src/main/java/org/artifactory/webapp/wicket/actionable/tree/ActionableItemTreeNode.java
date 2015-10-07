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

import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.model.HierarchicActionableItem;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Yoav Landman
 */
public class ActionableItemTreeNode<T extends ActionableItem> extends DefaultMutableTreeNode {

    private boolean leaf;

    public ActionableItemTreeNode(ActionableItem item) {
        super(item);
        typeCheck(item);
        leaf = item instanceof HierarchicActionableItem;
        setAllowsChildren(leaf);
    }

    @Override
    public ActionableItemTreeNode getParent() {
        return (ActionableItemTreeNode) super.getParent();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public T getUserObject() {
        return (T) super.getUserObject();
    }

    @Override
    public void setUserObject(Object userObject) {
        try {
            typeCheck(userObject);
            super.setUserObject(userObject);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Using a non ActionableItem user object is not allowed with " +
                            "ActionableItemTreeNode.", e);
        }
    }

    @Override
    public boolean isLeaf() {
        return leaf || (children != null && children.isEmpty());
    }

    void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    @SuppressWarnings({"UnusedDeclaration", "unchecked"})
    private void typeCheck(Object o) {
        try {
            T t = (T) o;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Using a non ActionableItem user object is not allowed with " +
                            "ActionableItemTreeNode.", e);
        }
    }
}
