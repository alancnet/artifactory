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

package org.artifactory.webapp.wicket.panel.tabbed.tab;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.wicket.disabledaddon.DisableLinkBehavior;

/**
 * Tab base class which implements the IHasEnableState interface
 *
 * @author Noam Tenne
 */
public abstract class BaseTab extends AbstractTab {

    public BaseTab(String title) {
        super(Model.of(title));
    }

    public BaseTab(IModel<String> title) {
        super(title);
    }

    public boolean isEnabled() {
        return true;
    }

    public void onNewTabLink(Component link) {
        if (!isEnabled()) {
            link.add(new DisableLinkBehavior());
        }
    }

    public void onNewTabItem(LoopItem item) {
    }
}
