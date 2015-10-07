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

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import java.util.Iterator;

/**
 * @author Yoav Aharoni
 */
class MasterDetailItemsStrategy implements IItemReuseStrategy {
    private MasterDetailTable table;

    MasterDetailItemsStrategy(MasterDetailTable table) {
        this.table = table;
    }

    @Override
    public Iterator getItems(final IItemFactory factory, final Iterator newModels, Iterator existingItems) {
        return new ItemIterator(newModels);
    }

    private class ItemIterator implements Iterator {
        private int index;
        private final Iterator newModels;

        public ItemIterator(Iterator newModels) {
            this.newModels = newModels;
            index = 0;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object next() {
            Object next = newModels.next();
            if (next != null && !(next instanceof IModel)) {
                throw new WicketRuntimeException("Expecting an instance of " +
                        IModel.class.getName() + ", got " + next.getClass().getName());
            }

            final IModel model = (IModel) next;
            Item item = table.newMasterRow(model, index);
            index++;
            return item;
        }

        @Override
        public boolean hasNext() {
            return newModels.hasNext();
        }
    }
}