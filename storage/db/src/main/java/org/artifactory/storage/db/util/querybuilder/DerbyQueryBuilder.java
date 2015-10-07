package org.artifactory.storage.db.util.querybuilder;

/**
 * @author Chen Keinan
 */
public class DerbyQueryBuilder extends BaseQueryBuilder {

    @Override
    public String uniqueBuild(String baseQuery, String sortBy, long offSet, long limit) {
        StringBuilder builder = new StringBuilder(baseQuery);
        if (offSet > 0) {
            builder.append("offset ").append(offSet).append(" rows ");
        }
        if (limit < Long.MAX_VALUE) {
            builder.append("fetch first ").append(limit).append(" rows only ");
        }
        return builder.toString();
    }

    @Override
    public boolean shouldAddOrderBy(Long offSet, Long limit) {
        return true;
    }
}
