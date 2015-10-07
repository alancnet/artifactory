package org.artifactory.storage.db.util.querybuilder;

import org.artifactory.api.context.ContextHelper;

/**
 * @author Chen Keinan
 */
public class QueryWriter {

    BaseQueryBuilder queryBuilder;
    private String distinct;
    private String fields;
    private String tables;
    private String conditions;
    private String groupBy;
    private String orderBy;
    private Long offSet;
    private Long limit;
    public QueryWriter() {
        queryBuilder = (BaseQueryBuilder) ContextHelper.get().beanForType(IQueryBuilder.class);
    }

    public QueryWriter distinct() {
        this.distinct = "distinct ";
        return this;
    }

    public QueryWriter select() {
        this.fields = " * ";
        return this;
    }

    public QueryWriter select(String fields) {
        this.fields = fields;
        return this;
    }

    public QueryWriter from(String tables) {
        this.tables = tables;
        return this;
    }

    public QueryWriter groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public QueryWriter where(String condition) {
        this.conditions = condition;
        return this;
    }

    public QueryWriter orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QueryWriter offset(Long offset) {
        offSet = offset;
        return this;
    }

    public QueryWriter limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public String build() {
        return queryBuilder.build(distinct, fields, tables, conditions, orderBy, groupBy, offSet, limit);
    }

    public void clear() {
        distinct = null;
        fields = null;
        tables = null;
        conditions = null;
        groupBy = null;
        orderBy = null;
        offSet = null;
        limit = null;
    }

    public void useExistingQuery(String baseQuery) {

    }
}

