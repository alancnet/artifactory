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

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.common.wicket.model.sitemap.SiteMap;

import static org.artifactory.common.wicket.component.panel.sidemenu.MenuItem.Status.*;

/**
 * @author Yoav Aharoni
 */
public class MenuItem extends Panel {
    private Status status;

    public MenuItem(String id, MenuNode menuNode, Class<? extends Page> currentPage) {
        super(id);
        Class<? extends Page> pageClass = menuNode.getPageClass();
        boolean enabled = menuNode.isEnabled();
        status = fetchStatus(menuNode, currentPage, enabled);

        // add link
        WebMarkupContainer link;
        if (pageClass == null) {
            link = new ToogleGroupLink("link", menuNode);
        } else {
            link = new BookmarkablePageLink<String>("link", pageClass);
        }
        link.setEnabled(enabled);
        add(link);

        // add label
        link.add(new Label("name", menuNode.getName()));


        // add menu node css
        String menuCssClass = menuNode.getCssClass();
        if (menuCssClass != null) {
            add(new CssClass(menuNode.getCssClass()));
        }

        // add status css
        String nodeType = menuNode.isLeaf() ? "menu-item" : "menu-group";
        String cssClass = nodeType + " " + nodeType + "-" + status.getCssClass();
        add(new CssClass(cssClass));

        link.add(new CssClass(status.getCssClass()));

        menuNode.onNewLink(link);
    }

    public Status getStatus() {
        return status;
    }

    private Status fetchStatus(MenuNode node, Class<? extends Page> currentPage, boolean enabled) {
        if (!enabled) {
            return DISABLED;
        }

        if (currentPage.equals(node.getPageClass())) {
            return SELECTED;
        }

        Boolean opened = node.isOpened();
        if (Boolean.TRUE.equals(opened)) {
            return OPENED;
        } else if (Boolean.FALSE.equals(opened)) {
            return ENABLED;
        }

        SiteMap siteMap = ((SiteMapAware) Application.get()).getSiteMap();
        MenuNode current = siteMap.getPageNode(currentPage);
        while (current != null) {
            if (current.equals(node)) {
                return OPENED;
            }
            current = current.getParent();
        }
        return ENABLED;
    }

    public enum Status {
        DISABLED("disabled"),
        ENABLED("enabled"),
        OPENED("opened"),
        SELECTED("selected");

        private String cssClass;

        Status(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }

        public boolean isOpened() {
            return equals(OPENED) || equals(SELECTED);
        }
    }

    private static class ToogleGroupLink extends WebMarkupContainer {
        private MenuNode menuNode;

        private ToogleGroupLink(String id, MenuNode menuNode) {
            super(id);
            this.menuNode = menuNode;
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("href", "#");
            if (isEnableAllowed() && isEnabled()) {
                tag.put("onclick", "return SubMenuPanel.toogleMenu('" + menuNode.getCookieName() + "', this);");
            } else {
                tag.put("onclick", "return false;");
            }
        }
    }
}