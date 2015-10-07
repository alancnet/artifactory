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

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.navigation.NavigationToolbarWithDropDown;
import org.artifactory.common.wicket.component.table.columns.AttachColumnListener;
import org.artifactory.common.wicket.component.table.groupable.header.AjaxGroupableHeadersToolbar;

import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class SortableTable<T> extends DataTable<T> {
    @SuppressWarnings({"unchecked"})
    public SortableTable(String id, final List<IColumn<T>> columns, ISortableDataProvider<T> dataProvider,
            int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
        add(new CssClass("data-table"));

        addTopToolbar(getDropDownNavToolbar());

        // add header
        AbstractToolbar headersToolbar = newHeadersToolbar();
        addTopToolbar(headersToolbar);

        // add bottom toolbars
        addBottomToolbar(new NoRecordsToolbar(this));

        notifyColumnsAttached();
    }

    public ISortableDataProvider<T> getSortableDataProvider() {
        return (ISortableDataProvider<T>) getDataProvider();
    }

    protected NavigationToolbarWithDropDown getDropDownNavToolbar() {
        return new NavigationToolbarWithDropDown(this, 1);
    }

    protected AbstractToolbar newHeadersToolbar() {
        return new AjaxGroupableHeadersToolbar(this, getSortableDataProvider());
    }

    @Override
    protected Item<T> newRowItem(String id, int index, IModel<T> model) {
        OddEvenItem<T> rowItem = new OddEvenItem<>(id, index, model);
        rowItem.add(new JavascriptEvent("onmouseover", "DomUtils.addHoverStyle(this);"));
        rowItem.add(new JavascriptEvent("onmouseout", "DomUtils.removeHoverStyle(this);"));
        return rowItem;
    }

    @Override
    protected Item<IColumn<T>> newCellItem(final String id, final int index, final IModel<IColumn<T>> model) {
        Item<IColumn<T>> item = super.newCellItem(id, index, model);
        if (index == 0) {
            item.add(new CssClass("first-cell"));
        } else if (index == getColumns().size() - 1) {
            item.add(new CssClass("last-cell"));
        }
        return item;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("cellpadding", "0");
        tag.put("cellspacing", "0");
    }

    @SuppressWarnings({"unchecked"})
    private void notifyColumnsAttached() {
        for (IColumn column : getColumns()) {
            if (column instanceof AttachColumnListener) {
                ((AttachColumnListener) column).onColumnAttached(this);
            }
        }
    }
}
