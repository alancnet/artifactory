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

package org.artifactory.webapp.wicket.page.config.repos;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.webapp.wicket.panel.tabbed.SubmittingTabbedPanel;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class RepoTabbedPanel extends SubmittingTabbedPanel {
    public RepoTabbedPanel(String id, List<ITab> tabs) {
        super(id, tabs);
        setOutputMarkupId(true);
    }

    @Override
    protected void onAjaxUpdate(AjaxRequestTarget target) {
        super.onAjaxUpdate(target);
        ModalHandler.bindHeightTo("modalScroll");
    }

    @Override
    public TabbedPanel setSelectedTab(int index) {
        setDefaultModelObject(index);
        ITab tab = getTabs().get(index);
        final Component component = tab.getPanel(TAB_PANEL_ID);
        component.add(new ScrollDivBehavior());
        addOrReplace(component);
        return this;
    }

    private static class ScrollDivBehavior extends AbstractBehavior {
        @Override
        public void beforeRender(Component component) {
            RequestCycle.get().getResponse().write("<div id='modalScroll'>");
        }

        @Override
        public void onRendered(Component component) {
            RequestCycle.get().getResponse().write("</div>");
        }
    }
}
