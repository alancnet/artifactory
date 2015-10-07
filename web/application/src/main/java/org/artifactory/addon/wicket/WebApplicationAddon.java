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

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.artifactory.addon.Addon;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.common.wicket.model.sitemap.MenuNode;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.wicket.page.base.BasePage;
import org.artifactory.webapp.wicket.page.config.general.BaseCustomizingPanel;

import java.util.Map;

/**
 * Login page addon.
 *
 * @author Yossi Shaul
 * @author Tomer Cohen
 */
public interface WebApplicationAddon extends Addon {
    String SUPER_USER_NAME = "super";

    /**
     * @param wicketId The link's wicket id
     * @return An link to an external authentication page.
     */
    IFormSubmittingComponent getLoginLink(String wicketId, Form form);

    AbstractLink getLogoutLink(String wicketId);

    AbstractLink getProfileLink(String wicketId);

    /**
     * Reset the password for a user.
     *
     * @param userName      The username
     * @param remoteAddress
     * @param resetPageUrl
     * @return info/error message
     */
    String resetPassword(String userName, String remoteAddress, String resetPageUrl);


    /**
     * @param componentClass The component's class
     * @return True if the instantiation of a component is allowed.
     */
    boolean isInstantiationAuthorized(Class componentClass);

    boolean isVisibilityAuthorized(Class componentClass);

    Class<? extends Page> getHomePage();

    Label getUptimeLabel(String wicketId);

    /**
     * Add version info components.
     *
     * @param headersMap
     */
    void addVersionInfo(WebMarkupContainer container, Map<String, String> headersMap);

    /**
     * @return Artifactory version string.
     */
    String getVersionInfo();


    String getPageTitle(BasePage page);

    /**
     * Returns an addon-customized import\export menu node
     *
     * @return Import\export menu node
     */
    MenuNode getImportExportMenuNode();

    /**
     * Returns an addon-customized security menu node
     *
     * @return Security menu node  @param webstartAddon
     */
    MenuNode getSecurityMenuNode(WebstartWebAddon webstartAddon);

    /**
     * Returns an addon-customized services menu node
     *
     * @return Services menu node
     */
    MenuNode getServicesMenuNode();

    /**
     * Returns an addon-customized configuration menu node
     *
     * @param propertiesWebAddon A properties addon instance
     * @param licensesWebAddon
     * @param blackDuckWebAddon
     * @return Configuration menu node
     */
    MenuNode getConfigurationMenuNode(PropertiesWebAddon propertiesWebAddon, LicensesWebAddon licensesWebAddon,
            BlackDuckWebAddon blackDuckWebAddon);

    /**
     * Returns an addon-customized advanced menu node
     *
     * @return Advanced menu node
     */
    MenuNode getAdvancedMenuNode();

    /**
     * Returns an addon-customized URL base label
     *
     * @param id Component wicket ID
     * @return URL base label
     */
    Label getUrlBaseLabel(String id);

    /**
     * Returns an addon-customized URL base text field
     *
     * @param id Component wicket id
     * @return URL base text field
     */
    TextField getUrlBaseTextField(String id);

    /**
     * Returns an addon-customized URL base help bubble
     *
     * @param id Component wicket id
     * @return URL base help bubble
     */
    HelpBubble getUrlBaseHelpBubble(String id);

    /**
     * Returns the addon customized installed addon information panel
     *
     * @param panelId ID to assign to panel
     * @return Web markup container
     */
    WebMarkupContainer getAddonsInfoPanel(String panelId);

    /**
     * Returns the Export search result panel for artifactory users and a dummy visable false for aol users
     *
     * @param panelId
     * @return FieldSetPanel
     */
    FieldSetPanel getExportResultPanel(String panelId, ActionableItem actionableItem);

    BaseCustomizingPanel getCustomizingPanel(String id, IModel model);
}
