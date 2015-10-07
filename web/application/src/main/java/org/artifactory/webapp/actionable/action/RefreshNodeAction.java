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

package org.artifactory.webapp.actionable.action;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.artifactory.webapp.actionable.RefreshableActionableItem;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;
import org.artifactory.webapp.wicket.actionable.tree.ActionableItemsTree;

/**
 * Refresh the tree node data and children recursively.
 *
 * @author Yossi Shaul
 */
public class RefreshNodeAction extends RepoAwareItemAction {

    public static final String ACTION_NAME = "Refresh";

    public RefreshNodeAction() {
        super(ACTION_NAME);
    }

    @Override
    public void onAction(RepoAwareItemEvent e) {
        AjaxRequestTarget target = e.getTarget();
        WebMarkupContainer nodePanelContainer = e.getTargetComponents().getNodePanelContainer();
        Component refreshableComponent = e.getTargetComponents().getRefreshableComponent();
        if (refreshableComponent instanceof ActionableItemsTree) {
            ActionableItemsTree tree = (ActionableItemsTree) refreshableComponent;
            RepoAwareActionableItem sourceItem = e.getSource();
            if (sourceItem instanceof RefreshableActionableItem) {
                RefreshableActionableItem refreshable = (RefreshableActionableItem) sourceItem;
                refreshable.refresh();
                tree.refreshAndExpandItemNode(refreshable);
            }
        }
        target.add(nodePanelContainer);
    }
}
