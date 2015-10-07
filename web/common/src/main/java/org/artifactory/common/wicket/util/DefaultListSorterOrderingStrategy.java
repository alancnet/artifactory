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

package org.artifactory.common.wicket.util;

import com.google.common.collect.Ordering;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

/**
 * Default list ordering strategy. Always uses the property comparator
 *
 * @author Noam Y. Tenne
 */
public class DefaultListSorterOrderingStrategy<T> implements ListSorterOrderingStrategy<T> {

    @Override
    public Ordering<T> createOrdering(SortParam sortParam) {
        return Ordering.<T>from(new PropertyComparator(
                new MutableSortDefinition(sortParam.getProperty(), true, sortParam.isAscending())));
    }
}
