package org.artifactory.storage.db.aql.sql.builder.query.sql;

import com.google.common.collect.Maps;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQueryElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.Criteria;
import org.artifactory.storage.db.aql.sql.builder.query.sql.type.*;
import org.artifactory.storage.db.util.querybuilder.QueryWriter;
import org.artifactory.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * The Class converts AqlQuery into sql query
 * Basically the query is ANSI SQL except the limit and the offset
 *
 * @author Gidi Shabat
 */
public class SqlQueryBuilder {
    private Map<AqlDomainEnum, BasicSqlGenerator> sqlGeneratorMap;

    public SqlQueryBuilder() {
        sqlGeneratorMap = Maps.newHashMap();
        sqlGeneratorMap.put(AqlDomainEnum.items, new ArtifactsSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.properties, new PropertiesSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.archives, new ArchiveSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.statistics, new StatisticsSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.artifacts, new BuildArtifactSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.dependencies, new BuildDependenciesSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.modules, new BuildModuleSqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.moduleProperties, new BuildModulePropertySqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.buildProperties, new BuildPropertySqlGenerator());
        sqlGeneratorMap.put(AqlDomainEnum.builds, new BuildSqlGenerator());
    }

    private static boolean isWhereClauseExist(AqlQuery aqlQuery) {
        List<AqlQueryElement> elements = aqlQuery.getAqlElements();
        for (AqlQueryElement element : elements) {
            if (element instanceof Criteria) {
                return true;
            }
        }
        return false;
    }

    public SqlQuery buildQuery(AqlQuery aqlQuery) throws AqlException {
        QueryWriter queryWriter = new QueryWriter();
        SqlQuery sqlQuery = new SqlQuery(aqlQuery.getDomain());
        AqlDomainEnum domainEnum = aqlQuery.getDomain();
        BasicSqlGenerator generator = sqlGeneratorMap.get(domainEnum);
        generateSqlQuery(aqlQuery, generator, sqlQuery, queryWriter);
        sqlQuery.setResultFields(aqlQuery.getResultFields());
        sqlQuery.setLimit(aqlQuery.getLimit());
        sqlQuery.setOffset(aqlQuery.getOffset());
        return sqlQuery;
    }

    private void generateSqlQuery(AqlQuery aqlQuery, BasicSqlGenerator handler, SqlQuery query, QueryWriter queryWriter)
            throws AqlException {
        // Generate the result part of the query
        queryWriter.select(handler.results(aqlQuery));
        queryWriter.distinct();
        // Generate the from part of the query
        queryWriter.from(handler.tables(aqlQuery));
        // Add where clause if needed
        boolean whereClause = SqlQueryBuilder.isWhereClauseExist(aqlQuery);
        if (whereClause) {
            Pair<String, List<Object>> filter = handler.conditions(aqlQuery);
            queryWriter.where(filter.getFirst());
            query.setParams(filter.getSecond());
        }
        // Generate the sort part of the query
        String sort = handler.sort(aqlQuery);
        if (sort != null) {
            queryWriter.orderBy(sort);
        }
        // Generate offset and limit
        long offset = aqlQuery.getOffset();
        long limit = aqlQuery.getLimit();
        queryWriter.offset(offset);
        queryWriter.limit(limit);
        query.setQuery(queryWriter.build());
    }
}
