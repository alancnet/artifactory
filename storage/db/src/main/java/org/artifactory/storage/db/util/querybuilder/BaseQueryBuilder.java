package org.artifactory.storage.db.util.querybuilder;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @author Chen Keinan
 */
public abstract class BaseQueryBuilder implements IQueryBuilder {
    public abstract String uniqueBuild(String baseQuery, String sortBy, long offSet, long limit);

    public String build(String distinct, String fields, String tables, String conditions, String orderBy, String groupBy, Long offSet, Long limit) {
        StringBuilder builder = new StringBuilder();
        // Start select section
        builder.append("select ");
        // set distinct
        if (!isBlank(distinct)) {
            builder.append("distinct ");
        }
        // Start append the fields section
        if (isBlank(fields)) {
            builder.append("* ");
        } else {
            builder.append(fields);
        }
        // Set from section
        builder.append("from ");
        if (isBlank(tables)) {
            throw new RuntimeException("Failed to build sql query. Reason: missing tables for the from section");
        } else {
            builder.append(tables);
        }
        // Set condition section
        if (!isBlank(conditions)) {
            builder.append("where ");
            builder.append(conditions);
        }
        // Set order section
        if (shouldAddOrderBy(offSet, limit) && !isBlank(orderBy)) {
            builder.append("order by ");
            builder.append(orderBy);
        }
        // Set group by section
        if (!isBlank(groupBy)) {
            builder.append("group by ");
            builder.append(groupBy);
        }
        String result;
        // handle special pagination behavior
        if (isPagination(offSet, limit)) {
            // Need special db type behaviour
            if (offSet == null) {
                offSet = 0l;
            }
            if (limit == null) {
                limit = Long.MAX_VALUE;
            }
            result = uniqueBuild(builder.toString(), orderBy, offSet, limit);
        } else {
            result = builder.toString();
        }
        return result;
    }

    public abstract boolean shouldAddOrderBy(Long offSet, Long limit);

    public boolean isPagination(Long offSet, Long limit) {
        return offSet != null && offSet > 0 || limit != null && limit < Long.MAX_VALUE;
    }
}

