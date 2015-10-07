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

package org.artifactory.common.wicket.component.table.groupable.column;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.AttachColumnListener;
import org.artifactory.common.wicket.component.table.columns.TitlePropertyColumn;
import org.artifactory.common.wicket.component.table.groupable.GroupableTable;
import org.artifactory.common.wicket.component.table.groupable.provider.GroupableDataProvider;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public class GroupableColumn<T extends Serializable> extends TitlePropertyColumn<T>
        implements IGroupableColumn<T>, AttachColumnListener<T> {

    public GroupableColumn(String title, String sortProperty, String propertyExpression) {
        super(title, sortProperty, propertyExpression);
    }

    public GroupableColumn(IModel<String> displayModel, String sortProperty, String propertyExpression) {
        super(displayModel.getObject(), sortProperty, propertyExpression);
    }

    @Override
    public String getGroupProperty() {
        return super.getSortProperty();
    }

    @Override
    public void onColumnAttached(SortableTable<T> table) {
        if (this instanceof IChoiceRenderer && table instanceof GroupableTable) {
            GroupableDataProvider<T> dataProvider = ((GroupableTable<T>) table).getGroupableDataProvider();
            dataProvider.setGroupRenderer(getGroupProperty(), (IChoiceRenderer<T>) this);
        }
    }
}