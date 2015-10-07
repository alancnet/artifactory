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

import org.apache.commons.lang.SerializationUtils;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.groupable.provider.GroupableDataProvider;

import java.util.Iterator;

/**
 * @author Yoav Aharoni
 */
public class GroupedItemsStrategy implements IItemReuseStrategy {
    private GroupableTable table;
    GroupedItemsStrategy(GroupableTable table) {
        this.table = table;
    }

    @Override
    public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory, final Iterator<IModel<T>> newModels,
            Iterator<Item<T>> existingItems) {
        return new Iterator<Item<T>>() {
            private int index = 0;

            private Object lastGroupValue;
            private Item<T> lastGroupItem;
            private IModel<T> lastGroupModel;

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return lastGroupModel != null || newModels.hasNext();
            }

            @Override
            @SuppressWarnings({"unchecked"})
            public Item<T> next() {
                // returned group item in last iteration, return saved model item
                if (lastGroupModel != null) {
                    Item<T> item = newRowItem(lastGroupModel);
                    lastGroupModel = null;
                    return item;
                }

                IModel<T> model = newModels.next();
                GroupableDataProvider provider = table.getGroupableDataProvider();
                SortParam groupParam = provider.getGroupParam();
                if (groupParam != null && model != null) {
                    String property = groupParam.getProperty();
                    IChoiceRenderer renderer = provider.getGroupRenderer(property);
                    T modelObject = model.getObject();
                    Object value = renderer.getIdValue(modelObject, index);
                    if (!value.equals(lastGroupValue)) {
                        lastGroupValue = value;
                        lastGroupModel = model;
                        GroupableTable modificationTable = (GroupableTable) SerializationUtils.clone(table);
                        lastGroupItem = modificationTable.newGroupRowItem("group" + index, index, model);
                        Item cellItem = modificationTable.newGroupCellItem("cells", 0, model);
                        lastGroupItem.add(cellItem);
                        modificationTable.populateGroupItem(cellItem, "cell", property, model);
                        return lastGroupItem;
                    }
                }
                return newRowItem(model);
            }

            @SuppressWarnings({"unchecked"})
            private Item<T> newRowItem(IModel<T> model) {
                Item<T> item = factory.newItem(index, model);
                if (lastGroupItem != null && !table.isGroupExpanded(lastGroupItem) &&
                        table.getGroupableDataProvider().getGroupParam() != null) {
                    item.add(new CssClass("row-collapsed"));
                }
                index++;
                return item;
            }
        };
    }
}
