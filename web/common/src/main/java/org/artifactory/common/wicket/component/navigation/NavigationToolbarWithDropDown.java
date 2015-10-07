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

package org.artifactory.common.wicket.component.navigation;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.component.table.SortableTable;

public class NavigationToolbarWithDropDown extends AbstractToolbar {

    private int displayRequiredPageAmount;

    public NavigationToolbarWithDropDown(SortableTable table, int displayRequiredPageAmount) {
        super(table);
        this.displayRequiredPageAmount = displayRequiredPageAmount;

        WebMarkupContainer span = new WebMarkupContainer("span");
        add(span);
        span.add(new AttributeModifier("colspan", table.getColumns().size()));

        span.add(newPagingNavigator("navigator", table));
        span.add(newNavigatorLabel("navigatorLabel"));
    }

    protected WebComponent newNavigatorLabel(String navigatorId) {
        IModel model = new AbstractReadOnlyModel() {
            @Override
            public Object getObject() {
                return getNavigatorText();
            }
        };

        return new Label(navigatorId, model);
    }

    protected String getNavigatorText() {
        return getString("navigator.text", new Model<DataTable<?>>(getTable()));
    }

    protected Panel newPagingNavigator(String navigatorId, DataTable table) {
        return new PagingNavigatorWithDropDown(navigatorId, table);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && getTable().getPageCount() > displayRequiredPageAmount;
    }
}