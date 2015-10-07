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

package org.artifactory.webapp.wicket.page.build.tabs.diff;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.artifactory.api.build.diff.BuildsDiffStatus;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.groupable.column.GroupableColumn;
import org.artifactory.webapp.wicket.page.build.actionable.BuildsDiffActionableItem;

/**
 * @author Shay Yaakov
 */
public class BuildDiffGroupableColumn extends GroupableColumn<BuildsDiffActionableItem> {

    public BuildDiffGroupableColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
        super(displayModel, sortProperty, propertyExpression);
    }

    @Override
    public void populateItem(Item<ICellPopulator<BuildsDiffActionableItem>> item, String componentId,
            final IModel<BuildsDiffActionableItem> model) {
        item.add(new Label(componentId, createLabelModel(model)) {
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                BuildsDiffStatus status = model.getObject().getStatus();
                if (BuildsDiffStatus.NEW.equals(status)) {
                    add(new CssClass("green-listed-label"));
                } else if (BuildsDiffStatus.UPDATED.equals(status)) {
                    add(new CssClass("blue-listed-label"));
                } else if (BuildsDiffStatus.REMOVED.equals(status)) {
                    add(new CssClass("light-gray-listed-label"));
                }
            }
        });
    }
}
