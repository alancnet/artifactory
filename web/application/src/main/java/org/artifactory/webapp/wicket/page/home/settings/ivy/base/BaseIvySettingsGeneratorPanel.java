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

package org.artifactory.webapp.wicket.page.home.settings.ivy.base;

import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.webapp.wicket.page.home.settings.BaseSettingsGeneratorPanel;

/**
 * A base settings generator panel for Ivy and Gradle since they share similar fields
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseIvySettingsGeneratorPanel extends BaseSettingsGeneratorPanel {

    /**
     * Main constructor
     *
     * @param id                     ID to assign to the panel
     * @param servletContextUrl      Running context URL
     * @param virtualRepoDescriptors Virtual repo descriptor list
     */
    protected BaseIvySettingsGeneratorPanel(String id, String servletContextUrl) {
        super(id, servletContextUrl);

        TitledBorder border = new TitledBorder("settingsBorder");

        TitledAjaxSubmitLink generateButton = getGenerateButton();

        form.add(new DefaultButtonBehavior(generateButton));
        border.add(form);
        add(border);
        add(generateButton);
    }
}
