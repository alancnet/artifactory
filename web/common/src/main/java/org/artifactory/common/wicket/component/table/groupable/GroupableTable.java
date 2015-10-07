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

package org.artifactory.common.wicket.component.table.groupable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.groupable.provider.GroupableDataProvider;
import org.artifactory.common.wicket.component.table.groupable.row.GroupRow;
import org.artifactory.common.wicket.component.table.toolbar.emptyrow.EmptyRowToolbar;
import org.artifactory.common.wicket.contributor.ResourcePackage;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class GroupableTable<T extends Serializable> extends SortableTable<T> {

    @SuppressWarnings({"unchecked"})
    public GroupableTable(String id, List<IColumn<T>> columns, GroupableDataProvider<T> dataProvider, int rowsPerPage) {
        super(id, addSpaceColumns(columns), dataProvider, rowsPerPage);

        add(new CssClass(new CssModel()));
        add(ResourcePackage.forJavaScript(GroupableTable.class));
        setItemReuseStrategy(new GroupedItemsStrategy(this));

        addBottomToolbar(new EmptyRowToolbar(this));
    }

    /**
     * Are Groups Expanded/Collapsed by default
     *
     * @param groupRowItem Group row item
     * @return true if expanded by default, else false
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public boolean isGroupExpanded(Item<T> groupRowItem) {
        return false;
    }

    protected Item newGroupRowItem(String id, int index, IModel<T> model) {
        Item<T> item = new Item<>(id, index, model);
        item.add(new CssClass("group-header-row"));
        if (isGroupExpanded(item)) {
            item.add(new CssClass("group-expanded"));
        } else {
            item.add(new CssClass("group-collapsed"));
        }
        return item;
    }

    protected Item newGroupCellItem(String id, int index, IModel<T> model) {
        Item item = new Item<>(id, index, model);
        String colspan = String.valueOf(getColumns().size());
        item.add(new AttributeModifier("colspan", colspan));
        item.add(new CssClass("first-cell last-cell"));
        item.add(new JavascriptEvent("onclick", "GroupableTable.collapseExpand(this);"));
        return item;
    }

    protected void populateGroupItem(Item cellItem, String componentId, String property, IModel<T> rowModel) {
        IChoiceRenderer<T> renderer = getGroupableDataProvider().getGroupRenderer(property);
        Object value = renderer.getDisplayValue(rowModel.getObject());
        Item rowItem = (Item) cellItem.getParent();
        int groupSize = getGroupableDataProvider().getGroupSize(
                rowItem.getIndex() + getCurrentPage() * getItemsPerPage());
        cellItem.add(new GroupRow(componentId, value, groupSize));
    }

    public GroupableDataProvider<T> getGroupableDataProvider() {
        return (GroupableDataProvider<T>) super.getDataProvider();
    }

    private class CssModel extends AbstractReadOnlyModel {
        @Override
        public Object getObject() {
            GroupableDataProvider provider = getGroupableDataProvider();
            boolean grouped = provider.getGroupParam() == null && provider.size() > 0;
            return grouped ? "groupable-table" : "groupable-table grouped";
        }
    }

    private static <T> List<IColumn<T>> addSpaceColumns(List<IColumn<T>> columns) {
        columns.add(0, new SpaceColumn<T>());
        columns.add(new SpaceColumn<T>());
        return columns;
    }

    private static class SpaceColumn<T> extends AbstractColumn<T> {
        public SpaceColumn() {
            super(Model.of(""));
        }

        @Override
        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
            cellItem.add(new Label(componentId, "."));
        }
    }
}
