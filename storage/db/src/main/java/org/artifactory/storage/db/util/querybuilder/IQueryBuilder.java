package org.artifactory.storage.db.util.querybuilder;

/**
 * @author Chen Keinan
 */
public interface IQueryBuilder {
    public abstract String uniqueBuild(String baseQuery, String fields, long offSet, long limit);

    public abstract boolean shouldAddOrderBy(Long offSet, Long limit);

}
