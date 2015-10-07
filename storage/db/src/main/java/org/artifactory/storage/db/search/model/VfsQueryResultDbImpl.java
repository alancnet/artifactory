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

import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryRow;

/**
 * Date: 8/5/11
 * Time: 7:15 PM
 *
 * @author Fred Simon
 */
public class VfsQueryResultDbImpl implements VfsQueryResult {
    private final Iterable<VfsQueryRow> queryResult;
    private long nbResult;

    public VfsQueryResultDbImpl(Iterable<VfsQueryRow> queryResult, long count) {
        this.queryResult = queryResult;
        this.nbResult = count;
    }

    @Override
    public long getCount() {
        return nbResult;
    }

    @Override
    public Iterable<VfsQueryRow> getAllRows() {
        return queryResult;
    }
}
