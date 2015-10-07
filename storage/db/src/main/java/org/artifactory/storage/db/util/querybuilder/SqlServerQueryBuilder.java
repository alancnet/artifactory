package org.artifactory.storage.db.util.querybuilder;

import org.apache.commons.lang.StringUtils;

/**
 * @author Chen Keinan
 */
public class SqlServerQueryBuilder extends BaseQueryBuilder {

    public String uniqueBuild(String baseQuery, String sortBy, long offSet, long limit) {
        StringBuilder builder;
        offSet += 1;
        long lng = Long.max(limit + offSet, Long.MAX_VALUE);
        builder = new StringBuilder();
        builder.append("Select bb.* from (");
        builder.append("Select aa.* ");
        String extraRowNumberField = " ,ROW_NUMBER() ";
        if (StringUtils.isBlank(sortBy)) {
            extraRowNumberField = extraRowNumberField + "OVER ( order by (SELECT 1)) AS RN ";
        } else {
            String[] split = StringUtils.split(sortBy, ",");
            String newSortBy = "";
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                newSortBy += "aa." + s.substring(s.indexOf('.') + 1) + ",";
            }
            newSortBy = newSortBy.substring(0, newSortBy.length() - 1);
            extraRowNumberField = extraRowNumberField + "OVER ( order by " + newSortBy + " ) AS RN ";
        }
        builder.append(extraRowNumberField);
        builder.append(" from ( ");
        builder.append(baseQuery).append(" ) aa");
        builder.append(" ) bb");
        builder.append(" Where bb.RN >= ").append(offSet);
        builder.append(" AND bb.RN < ").append(lng);
        builder.append(" ");
        return builder.toString();
    }

    @Override
    public boolean shouldAddOrderBy(Long offSet, Long limit) {
        return !isPagination(offSet, limit);
    }
}
