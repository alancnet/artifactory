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

package org.artifactory.common.wicket.component.table;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.CssClass;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public abstract class SingleSelectionTable<T> extends SortableTable<T> {
    private T selection;

    protected SingleSelectionTable(String id, List<IColumn<T>> columns,
            ISortableDataProvider<T> dataProvider, int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);

        add(new CssClass("selectable"));
        setOutputMarkupId(true);
    }

    protected void onRowSelected(T selection, final AjaxRequestTarget target) {
        target.add(this);
    }

    @Override
    protected Item<T> newRowItem(String id, int index, IModel<T> model) {
        Item<T> rowItem = super.newRowItem(id, index, model);
        if (model.getObject().equals(selection)) {
            rowItem.add(new CssClass("selected"));
        }
        return rowItem;
    }

    @Override
    protected Item<IColumn<T>> newCellItem(final String id, final int index, final IModel<IColumn<T>> model) {
        Item<IColumn<T>> item = super.newCellItem(id, index, model);
        if (model.getObject() instanceof PropertyColumn) {
            item.add(new SelectRowBehavior());
        }
        return item;
    }

    public T getSelection() {
        return selection;
    }

    private class SelectRowBehavior extends AjaxEventBehavior {
        private SelectRowBehavior() {
            super("onclick");
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected void onEvent(final AjaxRequestTarget target) {
            T rowObject = (T) getComponent().getParent().getParent().getDefaultModelObject();
            selection = rowObject;

            onRowSelected(rowObject, target);
        }
    }
}
