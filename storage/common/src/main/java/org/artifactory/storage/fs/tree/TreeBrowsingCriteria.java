/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.storage.fs.tree;

import org.artifactory.fs.ItemInfo;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * A criteria fo browsing the tree of nodes.
 *
 * @author Yossi Shaul
 */
public class TreeBrowsingCriteria {

    private boolean cacheChildren;
    private List<ItemNodeFilter> filters;
    private Comparator<ItemInfo> comparator;

    public TreeBrowsingCriteria(boolean cacheChildren,
            List<ItemNodeFilter> filters, Comparator<ItemInfo> comparator) {
        this.cacheChildren = cacheChildren;
        this.filters = filters;
        this.comparator = comparator;
    }

    public boolean isCacheChildren() {
        return cacheChildren;
    }

    @Nullable
    public List<ItemNodeFilter> getFilters() {
        return filters;
    }

    @Nullable
    public Comparator<ItemInfo> getComparator() {
        return comparator;
    }

}
