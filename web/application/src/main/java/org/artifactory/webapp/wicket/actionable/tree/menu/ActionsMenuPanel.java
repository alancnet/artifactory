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

package org.artifactory.webapp.wicket.actionable.tree.menu;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;
import org.artifactory.webapp.wicket.actionable.link.ActionLink;
import org.artifactory.webapp.wicket.actionable.tree.ActionableItemTreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A panel that contains the popup menu actions
 *
 * @author Yoav Landman
 */
public class ActionsMenuPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ActionsMenuPanel(String id, final ActionableItemTreeNode node) {
        super(id, Model.of(node));
        setOutputMarkupId(true);

        add(ResourcePackage.forJavaScript(ActionsMenuPanel.class));

        //Render the enabled actions for each node
        final ActionableItem actionableItem = node.getUserObject();
        Set<ItemAction> actions = actionableItem.getContextMenuActions();
        List<ItemAction> menuActions = new ArrayList<>(actions.size());
        //Filter non-menu actions
        for (ItemAction action : actions) {
            if (action.isEnabled()) {
                menuActions.add(action);
            }
        }
        ListView<ItemAction> menuItems = new ListView<ItemAction>("menuItem", menuActions) {
            @Override
            protected void populateItem(ListItem item) {
                final ItemAction action = (ItemAction) item.getDefaultModelObject();
                item.add(new ActionLink("link", action, actionableItem));
            }
        };
        WebMarkupContainer menu = new WebMarkupContainer("menu");
        menu.setMarkupId("contextMenu");
        menu.setOutputMarkupId(true);
        menu.add(menuItems);
        add(menu);
    }
}
