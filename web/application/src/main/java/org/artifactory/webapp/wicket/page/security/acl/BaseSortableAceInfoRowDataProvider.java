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

package org.artifactory.webapp.wicket.page.security.acl;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.util.ListPropertySorter;

import java.util.Iterator;
import java.util.List;

/**
 * @author Yossi Shaul
 */
public abstract class BaseSortableAceInfoRowDataProvider extends SortableDataProvider<AceInfoRow> {
    protected List<AceInfoRow> aces;

    public BaseSortableAceInfoRowDataProvider() {
        setSort("principal", SortOrder.ASCENDING);
    }

    public abstract void loadData();

    @Override
    public Iterator<AceInfoRow> iterator(int first, int count) {
        ListPropertySorter.sort(aces, getSort());
        List<AceInfoRow> list = aces.subList(first, first + count);
        return list.iterator();
    }

    @Override
    public int size() {
        return aces.size();
    }

    @Override
    public IModel<AceInfoRow> model(AceInfoRow object) {
        return new Model<>(object);
    }
}
