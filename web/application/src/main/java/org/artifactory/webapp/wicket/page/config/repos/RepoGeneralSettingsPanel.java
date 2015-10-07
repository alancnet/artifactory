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

package org.artifactory.webapp.wicket.page.config.repos;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;

/**
 * @author Yoav Aharoni
 */
public class RepoGeneralSettingsPanel extends Panel {
    public RepoGeneralSettingsPanel(String id) {
        super(id);

        // Repository description
        add(new TextArea("description"));
        add(new SchemaHelpBubble("description.help"));

        add(new TextArea("notes"));
        add(new SchemaHelpBubble("notes.help"));

        add(new TextArea("includesPattern"));
        add(new SchemaHelpBubble("includesPattern.help"));

        add(new TextArea("excludesPattern"));
        add(new SchemaHelpBubble("excludesPattern.help"));
    }
}