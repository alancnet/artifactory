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

package org.artifactory.addon.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.artifactory.addon.Addon;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.modal.panel.EditValueButtonRefreshBehavior;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.common.wicket.property.PropertyItem;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import java.util.List;

/**
 * Addon for property creation, addition and searchability
 *
 * @author Noam Tenne
 */
public interface PropertiesWebAddon extends Addon {

    /**
     * Returns the properties tab panel within a tab
     *
     * @param itemInfo selected item info
     * @return Tab containing the properties search panel
     */
    ITab getPropertiesTabPanel(final ItemInfo itemInfo);

    /**
     * Returns the properties tab panel for a search folder actionable item within a tab
     *
     * @param folderInfo selected item info
     * @param decendents Decendent files of this folder within the search result
     * @return Tab containing the properties search panel
     */
    ITab getSearchPropertiesTabPanel(final org.artifactory.fs.FolderInfo folderInfo,
            List<org.artifactory.fs.FileInfo> decendents);

    /**
     * Returns the property search page within a menu node
     *
     * @param nodeTitle Title to give to the node
     * @return Menu node containing the property search page
     */
    MenuNode getPropertySearchMenuNode(String nodeTitle);

    /**
     * Returns the property search panel within a tab
     *
     * @param parent   Parent page of the property search panel
     * @param tabTitle Title to give to the panel tab
     * @return Tab containing the property search panel
     */
    ITab getPropertySearchTabPanel(Page parent, String tabTitle);

    /**
     * Returns the property sets configuration page within a menu node
     *
     * @param nodeTitle Title to give to the node
     * @return Menu node containing the property sets page
     */
    MenuNode getPropertySetsPage(String nodeTitle);

    /**
     * Returns the property sets selection for the repo configuration
     *
     * @param tabTitle     Tab title
     * @param entity       Repo descriptor
     * @param propertySets Available property sets
     * @return Property sets selector tab if addon enabled. Disabled addon tab if not
     */
    ITab getRepoConfigPropertySetsTab(String tabTitle, final RealRepoDescriptor entity,
            final List<PropertySet> propertySets);

    BaseModalPanel getEditPropertyPanel(EditValueButtonRefreshBehavior refreshBehavior, PropertyItem propertyItem,
            List<PredefinedValue> values);

    /**
     * Returns the property management panel of a selected tree item
     *
     * @param panelId  ID of the panel to construct
     * @param itemInfo Info object of the currently selected tree item
     * @return Property management display component
     */
    Component getTreeItemPropertiesPanel(String panelId, ItemInfo itemInfo);

    /**
     * Get properties set on the path
     *
     * @param path
     */
    Properties getProperties(RepoPath path);

    /**
     * Removes all properties on the path according to the keys supplied
     */
    void removeProperties(RepoPath path, String propToDelete);
}