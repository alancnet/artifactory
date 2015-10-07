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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.zipentry;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.FilteredResourcesWebAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.webapp.actionable.model.ArchivedItemActionableItem;

/**
 * General info panel displayed in the artifacts tree browser.
 *
 * @author Yossi Shaul
 */
public class ZipEntryGeneralTabPanel extends Panel {
    public ZipEntryGeneralTabPanel(String id, ZipEntryInfo zipEntry, ArchivedItemActionableItem repoItem) {
        super(id);
        setOutputMarkupId(true);

        add(new ZipEntryPanel("generalInfoPanel", zipEntry));
        FilteredResourcesWebAddon filteredAddon = ContextHelper.get().beanForType(
                AddonsManager.class).addonByType(FilteredResourcesWebAddon.class);
        add(filteredAddon.getZipEntryActions("actionsPanel", repoItem));
    }
}
