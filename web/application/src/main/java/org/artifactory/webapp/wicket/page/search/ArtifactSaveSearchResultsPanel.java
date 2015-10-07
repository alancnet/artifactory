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

package org.artifactory.webapp.wicket.page.search;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.addon.AddonType;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;

/**
 * @author Noam Y. Tenne
 */
public class ArtifactSaveSearchResultsPanel extends SaveSearchResultsPanel {

    public ArtifactSaveSearchResultsPanel(String id, IModel model, AddonType requestingAddon) {
        super(id, model, requestingAddon);
    }

    @Override
    protected void addAdditionalFields(Form form) {
        StyledCheckbox completeVersionCheckbox =
                new StyledCheckbox("completeVersion", new PropertyModel<Boolean>(this, "completeVersion"));
        completeVersionCheckbox.setDefaultModelObject(Boolean.FALSE);
        form.add(completeVersionCheckbox);
        form.add(new HelpBubble("completeVersion.help",
                "For each search result, aggregate all artifacts belonging to the same artifact version (and group) \n" +
                        "under the saved search result, even if not directly found in the current search."));
    }
}