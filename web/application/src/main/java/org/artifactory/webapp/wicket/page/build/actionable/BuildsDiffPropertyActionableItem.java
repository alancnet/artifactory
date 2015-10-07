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

package org.artifactory.webapp.wicket.page.build.actionable;

import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.api.build.diff.BuildsDiffPropertyModel;
import org.artifactory.api.build.diff.BuildsDiffStatus;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.webapp.actionable.ActionableItemBase;
import org.artifactory.webapp.wicket.util.ItemCssClass;

/**
 * @author Shay Yaakov
 */
public class BuildsDiffPropertyActionableItem extends ActionableItemBase implements BuildsDiffActionableItem {

    private final BuildsDiffPropertyModel model;

    public BuildsDiffPropertyActionableItem(BuildsDiffPropertyModel model) {
        this.model = model;
    }

    @Override
    public Panel newItemDetailsPanel(String id) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return model.getDiffValue();
    }

    @Override
    public String getCssClass() {
        return ItemCssClass.doc.getCssClass();
    }

    @Override
    public void filterActions(AuthorizationService authService) {
    }

    @Override
    public BuildsDiffStatus getStatus() {
        return model.getStatus();
    }

    public BuildsDiffPropertyModel getModel() {
        return model;
    }
}
