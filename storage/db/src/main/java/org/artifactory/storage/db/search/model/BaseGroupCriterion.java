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

import java.util.List;

/**
 * Date: 8/6/11
 * Time: 12:56 PM
 *
 * @author Fred Simon
 */
class BaseGroupCriterion extends BaseVfsQueryCriterion {
    private final List<BaseVfsQueryCriterion> criteria = Lists.newArrayList();

    @Override
    public boolean hasPropertyFilter() {
        for (BaseVfsQueryCriterion criterion : criteria) {
            if (criterion.hasPropertyFilter()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasStatisticFilter() {
        for (BaseVfsQueryCriterion criterion : criteria) {
            if (criterion.hasStatisticFilter()) {
                return true;
            }
        }
        return false;
    }

    BaseVfsQueryCriterion addCriterion(BaseVfsQueryCriterion criterion) {
        if (criterion != null && criterion.isValid()) {
            criteria.add(criterion);
            return criterion;
        }
        return null;
    }

    @Override
    public boolean isValid() {
        // Always valid
        return true;
    }

    @Override
    protected VfsBoolType fill(DbSqlQueryBuilder query) {
        if (!criteria.isEmpty()) {
            query.usedPropertyCriteria = 0;
            query.addNextBoolIfNeeded();
            boolean wrapWithParentheses = criteria.size() > 1;
            if (wrapWithParentheses) {
                query.append('(');
            }
            for (BaseVfsQueryCriterion criterion : this.criteria) {
                query.addNextBoolIfNeeded();
                query.nextBool = criterion.fill(query);
            }
            if (wrapWithParentheses) {
                query.append(')');
            }
        }
        return nextBool;
    }
}
