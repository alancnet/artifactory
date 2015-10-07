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

package org.artifactory.common.wicket.component.table.groupable.cache;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public class GroupMetaData implements Serializable, Comparable<GroupMetaData> {
    private final Integer index;
    private final int size;

    public GroupMetaData(int begin, int end) {
        index = begin;
        size = end - begin;
    }

    public int getIndex() {
        return index;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupMetaData that = (GroupMetaData) o;
        return size == that.size && index.equals(that.index);
    }

    @Override
    public int hashCode() {
        int result = index.hashCode();
        result = 31 * result + size;
        return result;
    }

    @Override
    public int compareTo(GroupMetaData groupIndex) {
        return index.compareTo(groupIndex.index);
    }
}
