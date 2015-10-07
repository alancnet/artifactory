package org.artifactory.storage.db.aql.sql.builder.query.sql;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.DomainSensitiveField;

import java.util.List;

/**
 * The class represent Sql query, it also contains some simple convenient methods that helps to build the query
 *
 * @author Gidi Shabat
 */
public class SqlQuery {
    private String query = "";
    private List<Object> params = Lists.newArrayList();
    private List<DomainSensitiveField> resultFields;
    private long limit;
    private long offset;
    private AqlDomainEnum domain;

    public SqlQuery(AqlDomainEnum domain) {
        this.domain = domain;
    }

    public Object[] getQueryParams() {
        Object[] objects = new Object[params.size()];
        return params.toArray(objects);
    }

    public String getQueryString() {
        return query;
    }

    @Override
    public String toString() {
        return "SqlQuery{" +
                "query='" + query + '\'' +
                ", params=" + params +
                '}';
    }

    public List<DomainSensitiveField> getResultFields() {
        return resultFields;
    }

    public void setResultFields(List<DomainSensitiveField> resultFields) {
        this.resultFields = resultFields;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public AqlDomainEnum getDomain() {
        return domain;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
