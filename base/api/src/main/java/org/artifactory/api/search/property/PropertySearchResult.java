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

package org.artifactory.api.search.property;

import org.artifactory.api.search.SearchResultBase;
import org.artifactory.fs.ItemInfo;

/**
 * Holds property search result data.
 *
 * @author Noam Tenne
 */
public class PropertySearchResult extends SearchResultBase {

    public PropertySearchResult(ItemInfo itemInfo) {
        super(itemInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof SearchResultBase)) {
            return false;
        }
        return getItemInfo().equals(((SearchResultBase) o).getItemInfo());
    }

    @Override
    public int hashCode() {
        return getItemInfo().hashCode();
    }
}
