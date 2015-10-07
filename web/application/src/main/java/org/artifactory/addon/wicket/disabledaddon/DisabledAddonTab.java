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

package org.artifactory.addon.wicket.disabledaddon;

import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.IModel;
import org.artifactory.addon.AddonType;
import org.artifactory.webapp.wicket.panel.tabbed.tab.DisabledTab;

/**
 * Tab which is disabled by default and displays the given addon's promo
 *
 * @author Noam Tenne
 */
public class DisabledAddonTab extends DisabledTab {
    private AddonType addon;

    public DisabledAddonTab(IModel<String> title, AddonType addon) {
        super(title);
        this.addon = addon;
    }

    @Override
    public void onNewTabItem(LoopItem item) {
        super.onNewTabItem(item);
        item.add(new DisabledAddonBehavior(addon));
    }
}
