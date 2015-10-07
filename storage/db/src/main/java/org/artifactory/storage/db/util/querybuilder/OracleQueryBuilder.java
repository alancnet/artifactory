package org.artifactory.storage.db.util.querybuilder;

/**
 * @author Chen Keinan
 */
public class OracleQueryBuilder extends BaseQueryBuilder {

    @Override
    public String uniqueBuild(String baseQuery, String sortBy, long offSet, long limit) {
        long maxToFetch = Math.max(Long.MAX_VALUE,limit + offSet);
        StringBuilder builder;
        builder = new StringBuilder();
        builder.append("select * from( ");
        builder.append("select rownum rnum, inner_query.* from ( ").append(baseQuery).append(") inner_query ");
        builder.append("where ROWNUM <= ").append(maxToFetch).append(")");
        builder.append("where rnum > ").append(offSet).append(" ");
        return builder.toString();
    }

    @Override
    public boolean shouldAddOrderBy(Long offSet, Long limit) {
        return true;
    }
}
