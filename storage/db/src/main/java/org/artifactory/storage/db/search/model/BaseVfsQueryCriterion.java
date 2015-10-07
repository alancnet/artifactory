package org.artifactory.storage.db.search.model;

import org.artifactory.sapi.search.VfsBoolType;

/**
 * Date: 8/6/11
 * Time: 12:51 PM
 *
 * @author Fred Simon
 */
public abstract class BaseVfsQueryCriterion {
    VfsBoolType nextBool;

    public BaseVfsQueryCriterion() {
        this.nextBool = VfsBoolType.AND;
    }

    protected abstract VfsBoolType fill(DbSqlQueryBuilder query);

    public abstract boolean isValid();

    public abstract boolean hasPropertyFilter();

    public abstract boolean hasStatisticFilter();
}
