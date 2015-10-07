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

package org.artifactory.webapp.wicket.panel.tabbed;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.template.HtmlTemplate;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.webapp.wicket.panel.tabbed.tab.BaseTab;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class StyledTabbedPanel extends AjaxTabbedPanel {
    public StyledTabbedPanel(String id, List<ITab> tabs) {
        super(id, tabs);

        add(ResourcePackage.forJavaScript(StyledTabbedPanel.class));
        add(new CssClass("styled-tab-panel"));
        Component tabsContainer = get("tabs-container");
        tabsContainer.setOutputMarkupId(true);

        ScrollLink moveLeft = new ScrollLink("moveLeft");
        add(moveLeft);

        ScrollLink moveRight = new ScrollLink("moveRight");
        add(moveRight);

        HtmlTemplate initScript = new HtmlTemplate("initScript");
        add(initScript);

        initScript.setParameter("tabsContainerId", new PropertyModel(tabsContainer, "markupId"));
        initScript.setParameter("moveLeftId", new PropertyModel(moveLeft, "markupId"));
        initScript.setParameter("moveRightId", new PropertyModel(moveRight, "markupId"));
    }

    @Override
    protected final WebMarkupContainer newLink(String linkId, int index) {
        WebMarkupContainer link = createLink(linkId, index);

        Object tab = getTabs().get(index);
        if (tab instanceof BaseTab) {
            BaseTab baseTab = (BaseTab) tab;
            baseTab.onNewTabLink(link);
        }

        return link;
    }

    protected WebMarkupContainer createLink(String linkId, int index) {
        return super.newLink(linkId, index);
    }

    @Override
    protected LoopItem newTabContainer(int tabIndex) {
        LoopItem item = super.newTabContainer(tabIndex);
        Object tab = getTabs().get(tabIndex);
        if (tab instanceof BaseTab) {
            BaseTab baseTab = (BaseTab) tab;
            baseTab.onNewTabItem(item);
        }
        return item;
    }

    @Override
    protected void onAjaxUpdate(AjaxRequestTarget target) {
        super.onAjaxUpdate(target);
        AjaxUtils.refreshFeedback(target); //hack to clean feedback panel when switching tabs
    }

    private static class ScrollLink extends WebMarkupContainer {
        private ScrollLink(String id) {
            super(id);
            setOutputMarkupId(true);
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("href", "#");
        }
    }
}
