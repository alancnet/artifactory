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

package org.artifactory.storage.db.search.model;

import org.artifactory.sapi.search.InvalidQueryRuntimeException;
import org.artifactory.sapi.search.VfsComparatorType;
import org.artifactory.sapi.search.VfsQuery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Date: 8/6/11
 * Time: 12:01 PM
 *
 * @author Fred Simon
 */
class VfsQueryPathCriterionDbImpl extends VfsQueryCriterionDbImpl {
    VfsQueryPathCriterionDbImpl(@Nonnull VfsComparatorType comparator, @Nullable String value) {
        super("node_path");
        setComparator(comparator);
        switch (comparator) {
            case ANY:
                this.value = VfsQuery.ALL_PATH_VALUE;
                break;
            case EQUAL:
            case CONTAINS:
                this.value = value;
                break;
            default:
                throw new InvalidQueryRuntimeException("Path filter comparator type " + comparator + " not supported");
        }
    }
}
