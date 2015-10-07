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

package org.artifactory.webapp.wicket.page.config.general;

import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.WebApplicationAddon;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.util.validation.DateFormatValidator;

/**
 * General settings (server name, max upload, etc.) configuration panel.
 *
 * @author Yossi Shaul
 */
public class GeneralSettingsPanel extends TitledPanel {

    @SpringBean
    private AddonsManager addonsManager;

    public GeneralSettingsPanel(String id) {
        super(id);
        add(new CssClass("general-settings-panel"));

        add(new TextField("serverName"));

        WebApplicationAddon applicationAddon = addonsManager.addonByType(WebApplicationAddon.class);

        add(applicationAddon.getUrlBaseLabel("urlBaseLabel"));
        add(applicationAddon.getUrlBaseTextField("urlBase"));
        RequiredTextField<Integer> uploadSizeField = new RequiredTextField<>("fileUploadMaxSizeMb");
        uploadSizeField.add(new RangeValidator<>(0, Integer.MAX_VALUE));
        add(uploadSizeField);
        RequiredTextField<String> dateFormatField = new RequiredTextField<>("dateFormat");
        dateFormatField.add(new DateFormatValidator());
        add(dateFormatField);
        add(new StyledCheckbox("offlineMode"));
        add(new SchemaHelpBubble("serverName.help"));
        add(applicationAddon.getUrlBaseHelpBubble("urlBase.help"));
        add(new SchemaHelpBubble("fileUploadMaxSizeMb.help"));
        add(new SchemaHelpBubble("dateFormat.help"));
        add(new SchemaHelpBubble("offlineMode.help"));
    }

    @Override
    public String getTitle() {
        return "General Settings";
    }
}