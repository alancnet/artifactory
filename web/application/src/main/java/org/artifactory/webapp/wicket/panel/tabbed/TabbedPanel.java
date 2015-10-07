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

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.common.wicket.behavior.RenderJavaScript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yoav Landman
 */
public abstract class TabbedPanel extends Panel {

    protected TabbedPanel(String id) {
        super(id);
        List<ITab> tabs = new ArrayList<>();
        addTabs(tabs);
        final StyledTabbedPanel tabPanel = newTabbedPanel(tabs);
        tabPanel.add(new RenderJavaScript("Browser.fixTabPanel();"));
        add(tabPanel);
    }

    protected StyledTabbedPanel newTabbedPanel(List<ITab> tabs) {
        return new PersistentTabbedPanel("tabs", tabs);
    }

    protected abstract void addTabs(List<ITab> tabs);
}