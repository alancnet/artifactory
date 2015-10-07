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

package org.artifactory.common.wicket.model;

import org.apache.wicket.model.IModel;

import java.util.Collection;

/**
 * Boolean model for selecting item in a collection. If model value is true, item is contained in collection. Else, item
 * isn't contained in collection.
 *
 * @author Yoav Aharoni
 */
public class SelectedItemModel<T> implements IModel<Boolean> {
    private Collection<T> items;
    private T item;

    public SelectedItemModel(Collection<T> items, T item) {
        this.items = items;
        this.item = item;
    }

    @Override
    public Boolean getObject() {
        return items.contains(item);
    }

    @Override
    public void setObject(Boolean object) {
        if ((Boolean) object) {
            if (!items.contains(item)) {
                items.add(item);
            }
        } else {
            items.remove(item);
        }
    }

    @Override
    public void detach() {
    }
}
