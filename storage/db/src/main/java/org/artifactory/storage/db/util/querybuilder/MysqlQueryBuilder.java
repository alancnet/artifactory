package org.artifactory.storage.db.util.querybuilder;

/**
 * @author Chen Keinan
 */
public class MysqlQueryBuilder extends BaseQueryBuilder {

    @Override
    public String uniqueBuild(String baseQuery, String sortBy, long offSet, long limit) {
        if (offSet > 0) {
            return baseQuery + "limit " + offSet + "," + limit + " ";
        }
        if (limit < Long.MAX_VALUE) {
            return baseQuery + "limit " + limit + " ";
        }
        return baseQuery;
    }

    @Override
    public boolean shouldAddOrderBy(Long offSet, Long limit) {
        return true;
    }
}
