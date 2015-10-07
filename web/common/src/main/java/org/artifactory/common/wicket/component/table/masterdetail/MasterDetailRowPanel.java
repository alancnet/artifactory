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

package org.artifactory.common.wicket.component.table.masterdetail;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.beans.support.SortDefinition;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
@SuppressWarnings({"unchecked"})
class MasterDetailRowPanel<M extends Serializable, D extends Serializable> extends Panel {
    MasterDetailRowPanel(String id, M masterObject, MasterDetailTable<M, D> table) {
        super(id, new Model<>(masterObject));

        final boolean expanded = table.isMasterExpanded(masterObject);
        addMasterRow(table, masterObject, expanded);
        addDetailRows(table, masterObject, expanded);
    }

    private void sortList(List<D> list, SortParam sortParam) {
        if (sortParam != null && sortParam.getProperty().startsWith("detail.")) {
            ListPropertySorter.sort(list, sortParam);
            final String property = sortParam.getProperty().substring(7);
            final SortDefinition sortDefinition = new MutableSortDefinition(property, true, sortParam.isAscending());
            Collections.sort(list, new PropertyComparator(sortDefinition));
        }
    }

    private void addMasterRow(MasterDetailTable<M, D> table, M masterObject, boolean expanded) {
        // create master row
        final WebMarkupContainer row = new WebMarkupContainer("masterRow");
        add(row);

        row.add(new CssClass("group-header-row"));
        if (expanded) {
            row.add(new CssClass("group-expanded"));
        } else {
            row.add(new CssClass("group-collapsed"));
        }

        // create master cell
        final WebMarkupContainer cell = new WebMarkupContainer("cell");
        row.add(cell);
        String colspan = String.valueOf(table.getColumns().size());
        cell.add(new AttributeModifier("colspan", colspan));
        cell.add(new CssClass("first-cell last-cell"));
        cell.add(new ToggleMasterBehavior());

        // create master label
        final String label = table.getMasterLabel(masterObject);
        cell.add(new Label("label", label));
    }

    private void addDetailRows(MasterDetailTable<M, D> table, M masterObject, boolean expanded) {
        List<D> list = null;
        if (expanded) {
            // calculate inner rows only if this parent row is expanded
            list = table.getDetails(masterObject);
        }
        if (list == null) {
            add(new WebMarkupContainer("rows").setVisible(false));
            return;
        }

        final SortParam sortParam = getSortParam(table);
        sortList(list, sortParam);

        final ListDataProvider dataProvider = new DetailsDataProvider<>(list, masterObject);
        add(new DetailsGridView("rows", table.getColumns(), dataProvider));
    }

    private SortParam getSortParam(SortableTable table) {
        final SingleSortState sortState = (SingleSortState) table.getSortableDataProvider().getSortState();
        if (sortState == null) {
            return null;
        }
        return sortState.getSort();
    }

    private class ToggleMasterBehavior extends AjaxEventBehavior {
        public ToggleMasterBehavior() {
            super("onclick");
        }

        @Override
        protected void onEvent(AjaxRequestTarget target) {
            final MasterDetailTable table = findParent(MasterDetailTable.class);
            table.onMasterToggle(MasterDetailRowPanel.this, target);
        }
    }

    private static class DetailsGridView extends DataGridView {
        public DetailsGridView(String id, List<? extends ICellPopulator<?>> populators, IDataProvider dataProvider) {
            super(id, populators, dataProvider);
        }

        @Override
        protected Item newCellItem(String id, int index, IModel model) {
            Item item = new Item(id, index, model);
            final IColumn column = (IColumn) getPopulators().get(index);
            if (column instanceof IStyledColumn) {
                item.add(new CssClass(((IStyledColumn) column).getCssClass()));
            }
            return item;
        }

        @Override
        protected Item newRowItem(String id, int index, IModel model) {
            return new OddEvenItem(id, index, model);
        }
    }

}