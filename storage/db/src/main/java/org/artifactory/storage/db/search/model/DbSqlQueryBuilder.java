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

import com.google.common.collect.Lists;
import org.artifactory.sapi.search.VfsBoolType;
import org.artifactory.storage.db.util.JdbcHelper;

import java.util.List;

/**
 * Date: 11/27/12
 * Time: 5:33 PM
 *
 * @author freds
 */
class DbSqlQueryBuilder {
    final StringBuilder sqlQuery = new StringBuilder();
    final List<Object> params = Lists.newArrayList();
    VfsBoolType nextBool = null;
    int usedPropertyCriteria = 0;

    DbSqlQueryBuilder append(String sql) {
        sqlQuery.append(sql);
        return this;
    }

    DbSqlQueryBuilder append(char sql) {
        sqlQuery.append(sql);
        return this;
    }

    DbSqlQueryBuilder appendIfNeeded(char sql) {
        if (sqlQuery.charAt(sqlQuery.length() - 1) != sql) {
            sqlQuery.append(sql);
        }
        return this;
    }

    DbSqlQueryBuilder addParam(Object value) {
        if (value instanceof Iterable) {
            return addListParam((Iterable) value);
        }
        appendIfNeeded(' ');
        sqlQuery.append("? ");
        params.add(value);
        return this;
    }

    DbSqlQueryBuilder addListParam(Iterable<?> value) {
        appendIfNeeded(' ');
        sqlQuery.append("(#) ");
        params.add(value);
        return this;
    }

    public VfsBoolType addNextBoolIfNeeded() {
        appendIfNeeded(' ');
        if (nextBool == null) {
            return null;
        }
        // Keep an set to null => used
        VfsBoolType toAdd = nextBool;
        nextBool = null;
        sqlQuery.append(toAdd.name()).append(' ');
        return toAdd;
    }

    @Override
    public String toString() {
        return JdbcHelper.resolveQuery(sqlQuery.toString(), params.toArray(new Object[params.size()]));
    }

    public DbSqlQueryBuilder appendNodePropsTableName() {
        if (usedPropertyCriteria <= 1) {
            sqlQuery.append("node_props");
        } else {
            sqlQuery.append("node_props").append(usedPropertyCriteria);
        }
        return this;
    }
}
