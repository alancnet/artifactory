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

package org.artifactory.webapp.wicket.page.config.general.addon;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;

/**
 * Addon settings
 *
 * @author Yoav Landman
 */
public class AddonSettingsPanel extends TitledPanel {

    @SpringBean
    private AddonsManager addonsManager;

    public AddonSettingsPanel(String id) {
        super(id);
        add(new CssClass("general-settings-panel"));

        StyledCheckbox showAddonsInfoCheckbox = new StyledCheckbox("addons.showAddonsInfo");
        showAddonsInfoCheckbox.setTitle("Show Available Add-ons Info");
        add(showAddonsInfoCheckbox);

        add(new SchemaHelpBubble("addons.showAddonsInfo.help"));
    }

    @Override
    public String getTitle() {
        return "Add-on Settings";
    }
}