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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.artifactory.common.wicket.component.panel.shortcutkey.KeyEventHandler;
import org.artifactory.common.wicket.component.panel.shortcutkey.KeyListener;
import org.artifactory.common.wicket.component.panel.shortcutkey.KeyReleasedEvent;

import javax.swing.tree.TreeNode;

import static java.awt.event.KeyEvent.*;

/**
 * Handles keyboard events when browsing an ActionableItemsTree.
 *
 * @author Yossi Shaul
 */
public class TreeKeyEventHandler extends KeyEventHandler {

    public TreeKeyEventHandler(String id, final ActionableItemsTree tree) {
        super(id);

        addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyReleasedEvent e) {
                TreeNode selectedNode = tree.getSelectedNode();
                if (selectedNode == null) {
                    return;
                }

                ITreeState state = tree.getTreeState();
                boolean expanded = state.isNodeExpanded(selectedNode);
                AjaxRequestTarget target = e.getTarget();
                if (!expanded && !selectedNode.isLeaf() && selectedNode.getAllowsChildren()) {
                    state.expandNode(selectedNode);
                    tree.onJunctionLinkClicked(target, selectedNode);
                    target.add(tree);
                    target.appendJavaScript("Browser.scrollToSelectedNode();");
                } else {
                    tree.selectNode(selectedNode, tree.getNextTreeNode(selectedNode), target);
                }
            }
        }, VK_RIGHT, VK_ADD);

        addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyReleasedEvent e) {
                TreeNode selectedNode = tree.getSelectedNode();
                if (selectedNode == null) {
                    return;
                }

                ITreeState state = tree.getTreeState();
                boolean expanded = state.isNodeExpanded(selectedNode);
                AjaxRequestTarget target = e.getTarget();
                if (expanded) {
                    state.collapseNode(selectedNode);
                    tree.onJunctionLinkClicked(target, selectedNode);
                    target.add(tree);
                    target.appendJavaScript("Browser.scrollToSelectedNode();");
                } else {
                    tree.selectNode(selectedNode, tree.getPrevTreeNode(selectedNode), target);
                }
            }
        }, VK_LEFT, VK_SUBTRACT);

        addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyReleasedEvent e) {
                TreeNode selectedNode = tree.getSelectedNode();
                if (selectedNode != null) {
                    tree.selectNode(selectedNode, tree.getPrevTreeNode(selectedNode), e.getTarget());
                }

            }
        }, VK_UP);

        addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyReleasedEvent e) {
                TreeNode selectedNode = tree.getSelectedNode();
                if (selectedNode != null) {
                    tree.selectNode(selectedNode, tree.getNextTreeNode(selectedNode), e.getTarget());
                }
            }
        }, VK_DOWN);
    }
}
