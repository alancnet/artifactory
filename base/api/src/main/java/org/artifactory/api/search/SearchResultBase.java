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

package org.artifactory.api.search;

import org.apache.commons.lang.StringUtils;
import org.artifactory.fs.ItemInfo;

import java.io.File;

/**
 * @author Yoav Landman
 */
public abstract class SearchResultBase implements ItemSearchResult {
    public final ItemInfo itemInfo;

    public SearchResultBase(ItemInfo itemInfo) {
        this.itemInfo = itemInfo;
    }

    @Override
    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    @Override
    public String getName() {
        String itemName = itemInfo.getName();
        if (StringUtils.isEmpty(itemName)) {
            return getRepoKey();
        }
        return itemName;
    }

    public String getRelDirPath() {
        return new File(itemInfo.getRelPath()).getParent();
    }

    public String getRepoKey() {
        return itemInfo.getRepoKey();
    }

    /**
     * @return The relative path of the search result item
     */
    public String getRelativePath() {
        return itemInfo.getRelPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchResultBase)) {
            return false;
        }

        SearchResultBase base = (SearchResultBase) o;

        return !(itemInfo != null ? !itemInfo.equals(base.itemInfo) : base.itemInfo != null);

    }

    @Override
    public int hashCode() {
        return itemInfo != null ? itemInfo.hashCode() : 0;
    }
}