/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.util.NumberFormatter;

/**
 * A property column that displays a formatted long value using the pattern #,###. The value is right aligned.
 *
 * @author Yossi Shaul
 */
public class FormattedLongPropertyColumn<T> extends PropertyColumn<T> {
    public FormattedLongPropertyColumn(IModel<String> displayModel, String sortProperty,
            String propertyExpression) {
        super(displayModel, sortProperty, propertyExpression);
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
        item.add(new CssClass("right"));
        super.populateItem(item, componentId, rowModel);
    }

    @Override
    protected IModel<String> createLabelModel(IModel<T> rowModel) {
        IModel<?> labelModel = super.createLabelModel(rowModel);
        Long longValue = (Long) labelModel.getObject();
        return Model.of(NumberFormatter.formatLong(longValue));
    }
}
