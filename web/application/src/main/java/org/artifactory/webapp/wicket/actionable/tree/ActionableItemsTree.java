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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.Model;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.common.wicket.ajax.CancelDefaultDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.action.DeleteAction;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.actionable.action.ItemActionListener;
import org.artifactory.webapp.actionable.event.ItemEvent;
import org.artifactory.webapp.actionable.model.Compactable;
import org.artifactory.webapp.actionable.model.HierarchicActionableItem;
import org.artifactory.webapp.actionable.model.ZipFileActionableItem;
import org.artifactory.webapp.wicket.actionable.tree.menu.ActionsMenuPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * @author Yoav Landman
 */
public class ActionableItemsTree extends Tree implements ItemActionListener, Compactable {
    private static final Logger log = LoggerFactory.getLogger(ActionableItemsTree.class);

    private final ActionableItemsProvider itemsProvider;

    /**
     * Builds a tree and set the selected path to the input repo path. If the repoPath is null or not found, we use the
     * default view.
     *
     * @param id               The wicket id
     * @param itemsProvider    Actionable items provider
     * @param defaultSelection The path to select
     * @param compactAllowed   Is folder nodes compacting allowed
     */
    public ActionableItemsTree(String id, ActionableItemsProvider itemsProvider, DefaultTreeSelection defaultSelection,
            boolean compactAllowed) {
        super(id);
        this.itemsProvider = itemsProvider;
        setOutputMarkupId(true);

        setRootLess(true);
        setCompactAllowed(compactAllowed);
        selectPath(defaultSelection);
    }

    @Override
    public void setCompactAllowed(boolean compactAllowed) {
        HierarchicActionableItem root = this.itemsProvider.getRoot();
        if (root != null) {
            root.setCompactAllowed(compactAllowed);
        }
        ActionableItemTreeNode rootNode = new ActionableItemTreeNode(root);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        setDefaultModel(new Model<>(treeModel));
        List<? extends ActionableItem> children = this.itemsProvider.getChildren(root);
        setChildren(rootNode, children);
        getTreeState().expandNode(rootNode);
        if (rootNode.getChildCount() > 0) {
            selectNode(rootNode.getFirstChild());
        }
    }

    @Override
    protected void detachModel() {
        super.detachModel();
        detachNodes();
    }

    @SuppressWarnings({"unchecked"})
    private void detachNodes() {
        ActionableItemTreeNode root = (ActionableItemTreeNode) getTreeModel().getRoot();
        Enumeration<ActionableItemTreeNode> nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            ActionableItemTreeNode node = nodes.nextElement();
            ActionableItem userObject = node.getUserObject();
            if (userObject != null) {
                userObject.detach();
            }
        }
    }

    @Override
    public boolean isCompactAllowed() {
        return ((Compactable) getTreeModel().getRoot()).isCompactAllowed();
    }

    private void selectPath(DefaultTreeSelection defaultSelection) {
        if (defaultSelection == null) {
            return;
        }

        String treePath = defaultSelection.getDefaultSelectionTreePath();
        if (StringUtils.isNotBlank(treePath)) {
            try {
                // now build all the nodes on the way to the destination path and
                // expand only the nodes to the destination path
                DefaultTreeModel treeModel = getTreeModel();
                ActionableItemTreeNode parentNode = (ActionableItemTreeNode) treeModel.getRoot();
                String remainingPath = treePath;
                ActionableItemTreeNode currentNode = null;
                while (PathUtils.hasText(remainingPath)) {

                    // get deepest node for the path (will also take care of compacted paths)
                    currentNode = defaultSelection.getNodeAt(parentNode, remainingPath);
                    if (currentNode == parentNode) {
                        throw new ItemNotFoundRuntimeException(
                                format("Child node %s not found under %s",
                                        remainingPath, parentNode.getUserObject().getDisplayName()));
                    }

                    ActionableItem userObject = currentNode.getUserObject();
                    if (userObject instanceof HierarchicActionableItem &&
                            !(userObject instanceof ZipFileActionableItem)) {
                        // the node found is hierarchical, meaning it can have children
                        // so we get and create all the current node children
                        List<? extends ActionableItem> folderChildren = itemsProvider
                                .getChildren((HierarchicActionableItem) userObject);
                        setChildren(currentNode, folderChildren);
                        getTreeState().expandNode(currentNode);
                        parentNode = currentNode;
                    }

                    // subtract the resolved path from the remaining path
                    // we are currently relying on the display name as there is
                    // no better way to know if the node was compacted or not
                    String displayName = userObject.getDisplayName();
                    remainingPath = remainingPath.substring(displayName.length());
                    // just make sure we don't have '/' at the beginning
                    remainingPath = PathUtils.trimLeadingSlashes(remainingPath);
                }

                // everything went well and we have the destination node. now select it
                selectNode(currentNode);

            } catch (Exception e) {
                String message = "Unable to find path: " + treePath;
                error(message);
                log.error(message, e);
                getTreeState().collapseAll();
            }
        }
    }

    private DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel) getDefaultModelObject();
    }

    @Override
    protected Component newNodeIcon(MarkupContainer parent, String id, TreeNode node) {
        WebMarkupContainer icon = new WebMarkupContainer(id);
        ActionableItemTreeNode treeNode = (ActionableItemTreeNode) node;
        ActionableItem item = treeNode.getUserObject();
        icon.add(new CssClass(item.getCssClass()));
        return icon;
    }

    @Override
    protected void populateTreeItem(final WebMarkupContainer item, int level) {
        super.populateTreeItem(item, level);
        item.get("nodeLink:label").add(new CssClass("node-label"));

        item.get("nodeLink").add(new AjaxEventBehavior("oncontextmenu") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                onContextMenu(item, target);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new CancelDefaultDecorator();
            }
        });
    }

    protected void onContextMenu(Component item, AjaxRequestTarget target) {
        ActionableItemTreeNode node = (ActionableItemTreeNode) item.getDefaultModelObject();

        // check at least one action is enabled
        Set<ItemAction> actions = node.getUserObject().getContextMenuActions();
        for (ItemAction action : actions) {
            if (action.isEnabled()) {
                showContextMenu(item, node, target);
                return;
            }
        }
    }

    private void showContextMenu(Component item, ActionableItemTreeNode node, AjaxRequestTarget target) {
        ActionsMenuPanel menuPanel = new ActionsMenuPanel("contextMenu", node);
        getParent().replace(menuPanel);
        target.add(menuPanel);
        target.appendJavaScript(format("ActionsMenuPanel.show('%s');", item.getMarkupId()));
    }

    /**
     * User clicked on the junction link to expand/collapse a (hierarchical) node. In case of expand, refresh the node
     * children.
     *
     * @param node The node to expand/collapse.
     */
    @Override
    public void onJunctionLinkClicked(AjaxRequestTarget target, TreeNode node) {
        super.onJunctionLinkClicked(target, node);
        boolean expanded = isNodeExpanded(node);
        if (expanded) {
            refreshChildren((ActionableItemTreeNode) node);
        }
        adjustLayout(target);
    }

    public void adjustLayout(AjaxRequestTarget target) {
        target.appendJavaScript("dijit.byId('browseTree').layout();");
    }

    private void refreshChildren(ActionableItemTreeNode actionableItemTreeNode) {
        HierarchicActionableItem item = (HierarchicActionableItem) actionableItemTreeNode.getUserObject();
        debugGetChildren(item, "Getting children for");
        List<? extends ActionableItem> children = itemsProvider.getChildren(item);
        setChildren(actionableItemTreeNode, children);
        debugGetChildren(item, "Got children for");
    }

    @Override
    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node) {
        super.onNodeLinkClicked(target, node);
        selectNode(node);
        target.add(itemsProvider.getItemDisplayPanel());
    }

    private void selectNode(TreeNode node) {
        getTreeState().selectNode(node, true);
        refreshDisplayPanel();
    }

    public Panel refreshDisplayPanel() {
        ActionableItemTreeNode mutableTreeNode = (ActionableItemTreeNode) getSelectedNode();
        ActionableItem item = mutableTreeNode.getUserObject();
        Panel oldDisplayPanel = itemsProvider.getItemDisplayPanel();
        Panel newDisplayPanel = item.newItemDetailsPanel(oldDisplayPanel.getId());
        newDisplayPanel.setOutputMarkupId(true);
        oldDisplayPanel.replaceWith(newDisplayPanel);
        itemsProvider.setItemDisplayPanel(newDisplayPanel);
        return newDisplayPanel;
    }

    @Override
    protected Component newJunctionLink(MarkupContainer parent, String id, String imageId, TreeNode node) {
        //Collapse empty nodes
        ActionableItemTreeNode mutableTreeNode = (ActionableItemTreeNode) node;
        ActionableItem userObject = mutableTreeNode.getUserObject();
        if (userObject instanceof HierarchicActionableItem) {
            boolean hasChildren = itemsProvider.hasChildren((HierarchicActionableItem) userObject);
            //Must be set before the call to super
            mutableTreeNode.setLeaf(!hasChildren);
        } else {
            mutableTreeNode.setLeaf(true);
        }
        return super.newJunctionLink(parent, id, imageId, node);
    }

    @Override
    public void onTargetRespond(AjaxRequestTarget target) {
        log.debug("Beginning tree update ajax response.");
        super.onTargetRespond(target);
        log.debug("Finished tree update ajax response.");
    }

    @Override
    public void actionPerformed(ItemEvent e) {
        String command = e.getActionCommand();

        if (DeleteAction.ACTION_NAME.equals(command) || "Discard from Results".equals(command)) {

            ActionableItem item = e.getSource();
            ActionableItemTreeNode itemNode = searchForNodeByItem(item);
            if (itemNode == null || itemNode.getParent() == null) {
                return;
            }

            ActionableItemTreeNode parentNode = itemNode.getParent();

            if (parentNode.isRoot()) {
                // if this is a repository node just remove all its children, not the node itself
                itemNode.removeAllChildren();
            } else {
                itemNode.removeFromParent();
            }

            expandNode(e.getTarget(), parentNode);
        }
    }

    private void expandNode(AjaxRequestTarget target, ActionableItemTreeNode node) {
        getTreeState().expandNode(node);
        target.add(this);
        adjustLayout(target);
    }

    /**
     * @param item Item to look for
     * @return Tree node containing this item as the user object, null if not found
     */
    public ActionableItemTreeNode searchForNodeByItem(ActionableItem item) {
        DefaultTreeModel model = getTreeModel();
        ActionableItemTreeNode root = (ActionableItemTreeNode) model.getRoot();
        return searchForNodeByItem(root, item);
    }

    /**
     * Removes the node containing the item from its parent node and refreshe the tree.
     *
     * @param item The item to look for
     */
    public void removeItemNodeFromParent(ActionableItem item) {
        ActionableItemTreeNode node = searchForNodeByItem(item);
        if (node != null && node.getParent() != null) {
            ActionableItemTreeNode parent = node.getParent();
            node.removeFromParent();
            if (AjaxRequestTarget.get() != null) {
                expandNode(AjaxRequestTarget.get(), parent);
            }

        }
    }

    /**
     * Collapse the parent node of the node containing the item and refresh the tree.
     *
     * @param item The item the node contains
     */
    public void collapseItemNode(ActionableItem item) {
        ActionableItemTreeNode node = searchForNodeByItem(item);
        if (node != null && node.getParent() != null) {
            ActionableItemTreeNode parent = node.getParent();
            getTreeState().collapseNode(node);
            if (AjaxRequestTarget.get() != null) {
                expandNode(AjaxRequestTarget.get(), parent);
            }
        }
    }

    /**
     * Expand the node and refresh the node children.
     *
     * @param item The item the node contains
     */
    public void refreshAndExpandItemNode(ActionableItem item) {
        ActionableItemTreeNode node = searchForNodeByItem(item);
        if (AjaxRequestTarget.get() != null) {
            getTreeState().collapseNode(node);
            refreshChildren(node);
            expandNode(AjaxRequestTarget.get(), node);
        }
    }

    private ActionableItemTreeNode searchForNodeByItem(ActionableItemTreeNode node, ActionableItem item) {
        if (node.getUserObject().equals(item)) {
            // found the node containing the input item
            return node;
        } else if (node.getChildCount() <= 0) {
            // not this node and doesn't have any children, return null
            return null;
        } else {
            // search in children
            Enumeration children = node.children();
            while (children.hasMoreElements()) {
                ActionableItemTreeNode result =
                        searchForNodeByItem((ActionableItemTreeNode) children.nextElement(), item);
                if (result != null) {
                    // node found under the current children, return it
                    return result;
                }
            }
            // not found
            return null;
        }
    }

    public void selectNode(TreeNode selectedNode, TreeNode newSelection, AjaxRequestTarget target) {
        ITreeState state = getTreeState();
        if (selectedNode != null && newSelection != null) {
            state.selectNode(selectedNode, false);
            state.selectNode(newSelection, true);
            target.add(this);
            selectNode(newSelection);
            target.add(itemsProvider.getItemDisplayPanel());
            target.appendJavaScript("Browser.scrollToSelectedNode();");
        }
    }

    public TreeNode getNextTreeNode(TreeNode node) {
        ITreeState state = getTreeState();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return null;
        }
        if (!node.isLeaf() && node.getAllowsChildren() && state.isNodeExpanded(node)) {
            return node.getChildAt(0);
        }

        TreeNode nextNode = parent.getChildAfter(node);
        if (nextNode == null) {
            return getNextParent(parent);
        }
        return nextNode;
    }

    public TreeNode getNextParent(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return null;
        }
        TreeNode nextNode = parent.getChildAfter(node);
        if (nextNode == null) {
            return getNextParent(parent);
        }
        return nextNode;
    }

    public TreeNode getPrevTreeNode(TreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent == null) {
            return null;
        }

        TreeNode prevNode = parent.getChildBefore(node);
        if (prevNode != null) {
            ITreeState state = getTreeState();
            node = prevNode;
            while (!node.isLeaf() && node.getAllowsChildren() && state.isNodeExpanded(node)) {
                node = node.getChildAt(node.getChildCount() - 1);
            }
            return node;
        }

        DefaultTreeModel treeModel = getTreeModel();
        if (parent == treeModel.getRoot()) {
            return null;
        }
        return parent;
    }

    @SuppressWarnings({"unchecked"})
    public TreeNode getSelectedNode() {
        Collection<Object> selectedNodes = getTreeState().getSelectedNodes();
        if (selectedNodes.isEmpty()) {
            return null;
        }
        return (TreeNode) selectedNodes.iterator().next();
    }

    private static void setChildren(ActionableItemTreeNode node, List<? extends ActionableItem> children) {
        node.removeAllChildren();
        for (ActionableItem child : children) {
            ActionableItemTreeNode newChildNode = new ActionableItemTreeNode(child);
            node.add(newChildNode);
        }
    }

    private static void debugGetChildren(HierarchicActionableItem item, String msg) {
        if (!log.isDebugEnabled()) {
            return;
        }
        if (item instanceof RepoAwareActionableItem) {
            RepoAwareActionableItem raai = (RepoAwareActionableItem) item;
            RepoPath repoPath = raai.getRepoPath();
            log.debug(msg + " '" + repoPath + "'...");
        }
    }
}
