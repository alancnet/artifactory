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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.artifactory.addon.Addon;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.request.Request;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.action.ItemAction;

import java.io.Reader;

/**
 * Filtered resources web functionality interface
 *
 * @author Noam Y. Tenne
 */
public interface FilteredResourcesWebAddon extends Addon {

    /**
     * Returns the checkbox that controls the "filtered" state of an item
     *
     * @param componentId Checkbox ID
     * @param info        Selected item info
     * @return Checkbox
     */
    Component getFilteredResourceCheckbox(String componentId, ItemInfo info);

    /**
     * Returns a border containing the build-tool settings provisioning controls
     *
     * @param id             Border ID
     * @param form           Modal panel form
     * @param content        Editable text area
     * @param saveToFileName Default settings file name
     * @return Border
     */
    Component getSettingsProvisioningBorder(String id, Form form, TextArea<String> content, String saveToFileName);

    /**
     * Returns the current user's credentials template to place within generated settings
     *
     * @param escape True if the password should be escaped for use within XML
     * @return Credentials template
     */
    String getGeneratedSettingsUserCredentialsTemplate(boolean escape);

    String getGeneratedSettingsUsernameTemplate();

    String filterResource(Request request, Properties contextProperties, Reader reader) throws Exception;

    Component getZipEntryActions(String wicketId, ActionableItem repoItem);

    ItemAction getZipEntryDownloadAction();
}
