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

package org.artifactory.common.wicket.component.panel.sidemenu;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.model.sitemap.MenuNode;

import java.util.List;

/**
 * @author valdimiry
 */
public class SubMenuPanel extends Panel {
    public SubMenuPanel(String id, List<MenuNode> menuNodes, Class<? extends Page> currentPage) {
        super(id);
        setOutputMarkupId(true);
        add(ResourcePackage.forJavaScript(SubMenuPanel.class));

        RepeatingView items = new RepeatingView("menuItem");
        add(items);

        for (MenuNode menuNode : menuNodes) {
            MenuItem menuItem = new MenuItem(items.newChildId(), menuNode, currentPage);
            items.add(menuItem);

            if (!menuNode.isLeaf()) {
                SubMenuPanel subMenuPanel = new SubMenuPanel(items.newChildId(), menuNode.getChildren(), currentPage);
                items.add(subMenuPanel);

                boolean opened = menuItem.getStatus().isOpened();
                String cssClass = opened ? "sub-menu-opened" : "sub-menu-closed";
                subMenuPanel.add(new CssClass(cssClass));
            }
        }

        if (menuNodes.isEmpty()) {
            setVisible(false);
        }
    }
}
