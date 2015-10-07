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

package org.artifactory.common.wicket.component.table.columns;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.behavior.CssClass;

/**
 * A property column that expects property expression that returns boolean value.
 *
 * @author Yoav Aharoni
 */
public class BooleanColumn<T> extends TitlePropertyColumn<T> {
    public BooleanColumn(String title, String propertyExpression) {
        super(title, propertyExpression);
    }

    public BooleanColumn(String title, String sortProperty, String propertyExpression) {
        super(title, sortProperty, propertyExpression);
    }

    public BooleanColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
        super(displayModel.getObject(), sortProperty, propertyExpression);
    }

    public BooleanColumn(IModel<String> displayModel, String propertyExpression) {
        super(displayModel.getObject(), propertyExpression);
    }

    /**
     * @param item        The cell item
     * @param componentId The component id
     * @param model       The raw model, expected to return boolean value for the column property expression.
     */
    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> model) {
        PropertyModel<Boolean> booleanModel = new PropertyModel<>(model, getPropertyExpression());
        Boolean value = booleanModel.getObject();
        Label label = new Label(componentId, "<span>" + value + "</span>");
        label.setEscapeModelStrings(false);
        item.add(label);

        if (value != null) {
            item.add(new CssClass(value.toString().toLowerCase()));
        }
    }

    @Override
    public String getCssClass() {
        return "BooleanColumn";
    }
}
