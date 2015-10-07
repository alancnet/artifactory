package org.artifactory.storage.db.aql.service;

import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.db.aql.dao.AqlDao;
import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlApiToAqlAdapter;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.ParserToAqlAdapter;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlQueryBuilder;
import org.artifactory.storage.db.aql.sql.result.AqlEagerResultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Execute the Aql queries by processing the three Aql steps one after the other:
 * Step 1 Convert the AqlApi or the parser result into AqlQuery.
 * Step 2 Convert the AqlQuery into SqlQuery.
 * Step 3 Execute the SqlQuery and return the results.
 *
 * @author Gidi Shabat
 */
@Service
public class AqlServiceImpl implements AqlService {
    private static final Logger log = LoggerFactory.getLogger(AqlServiceImpl.class);

    @Autowired
    private AqlDao aqlDao;
    @Autowired
    private StorageProperties storageProperties;

    private AqlParser parser;
    private ParserToAqlAdapter parserToAqlAdapter;
    private AqlApiToAqlAdapter aqlApiToAqlAdapter;
    private SqlQueryBuilder sqlQueryBuilder;
    private AqlQueryOptimizer optimizer;
    private AqlQueryValidator validator;
    private AqlQueryDecorator decorator;
    private AqlPermissionProvider permissionProvider=new AqlPermissionProviderImpl();

    @PostConstruct
    private void initDb(){
        // The parser is constructed by many internal elements therefore we create it once and then reuse it.
        // Please note that it doesn't really have state therefore we can use it simultaneously
        // TODO init the parser eagerly here not lazy
        parser = new AqlParser();
        parserToAqlAdapter = new ParserToAqlAdapter();
        sqlQueryBuilder = new SqlQueryBuilder();
        aqlApiToAqlAdapter = new AqlApiToAqlAdapter();
        optimizer = new AqlQueryOptimizer(storageProperties.getDbType());
        validator = new AqlQueryValidator();
        decorator = new AqlQueryDecorator();
    }

    /**
     * Converts the Json query into SQL query and executes the query eagerly
     */
    @Override
    public AqlEagerResult executeQueryEager(String query) {
        log.debug("Processing textual AqlApi query: {}", query);
        ParserElementResultContainer parserResult = parser.parse(query);
        return executeQueryEager(parserResult);
    }

    /**
     * Converts the Json query into SQL query and executes the query lazy
     */
    @Override
    public AqlLazyResult executeQueryLazy(String query) {
        log.debug("Processing textual AqlApi query: {}", query);
        ParserElementResultContainer parserResult = parser.parse(query);
        return executeQueryLazy(parserResult);
    }

    /**
     * Converts the API's AqlApi query into SQL query and executes the query eagerly
     */
    @Override
    public <T extends AqlRowResult> AqlEagerResult<T> executeQueryEager(AqlBase<? extends AqlBase, T> aql) {
        log.debug("Processing API AqlApi query");
        AqlQuery aqlQuery = aqlApiToAqlAdapter.toAqlModel(aql);
        optimizer.optimize(aqlQuery);
        return (AqlEagerResultImpl<T>) getAqlQueryResult(aqlQuery);
    }

    @Override
    public AqlLazyResult executeQueryLazy(AqlBase aql) {
        log.debug("Processing API AqlApi query");
        AqlQuery aqlQuery = aqlApiToAqlAdapter.toAqlModel(aql);
        optimizer.optimize(aqlQuery);
        return getAqlQueryStreamResult(aqlQuery);
    }

    /**
     * Converts the parser elements into AqlApi query, convert the AqlApi query to sql and executes the query eagerly
     */
    private AqlEagerResult executeQueryEager(ParserElementResultContainer parserResult) {
        log.trace("Converting the parser result into AqlApi query");
        AqlQuery aqlQuery = parserToAqlAdapter.toAqlModel(parserResult);
        optimizer.optimize(aqlQuery);
        validator.validate(aqlQuery,permissionProvider);
        decorator.decorate(aqlQuery);
        log.trace("Successfully finished to convert the parser result into AqlApi query");
        return getAqlQueryResult(aqlQuery);
    }

    /**
     * Converts the parser elements into AqlApi query and executes the query lazy
     */
    private AqlLazyResult executeQueryLazy(ParserElementResultContainer parserResult) {
        log.trace("Converting the parser result into AqlApi query");
        AqlQuery aqlQuery = parserToAqlAdapter.toAqlModel(parserResult);
        optimizer.optimize(aqlQuery);
        validator.validate(aqlQuery,permissionProvider);
        decorator.decorate(aqlQuery);
        log.trace("Successfully finished to convert the parser result into AqlApi query");
        return getAqlQueryStreamResult(aqlQuery);
    }

    /**
     * Converts the AqlApi query into SQL query and executes the query eagerly
     */
    private AqlEagerResult getAqlQueryResult(AqlQuery aqlQuery) {
        log.trace("Converting the AqlApi query into SQL query: {}", aqlQuery);
        SqlQuery sqlQuery = sqlQueryBuilder.buildQuery(aqlQuery);
        log.trace("Successfully finished to convert the parser result into the following SQL query '{}'", sqlQuery);
        log.debug("processing the following SQL query: {}", sqlQuery);
        AqlEagerResultImpl aqlQueryResult = aqlDao.executeQueryEager(sqlQuery);
        log.debug("Successfully finished to process SQL query with the following size: {}", aqlQueryResult.getSize());
        return aqlQueryResult;
    }

    private AqlLazyResult getAqlQueryStreamResult(AqlQuery aqlQuery) {
        log.trace("Converting the AqlApi query into SQL qury: {}", aqlQuery);
        SqlQuery sqlQuery = sqlQueryBuilder.buildQuery(aqlQuery);
        log.trace("Successfully finished to convert the parser result into the following SQL query '{}'", sqlQuery);
        log.debug("processing the following SQL query: {}", sqlQuery);
        AqlLazyResult aqlQueryStreamResult = aqlDao.executeQueryLazy(sqlQuery, permissionProvider);
        log.debug("Successfully finished to process SQL query (lazy)");
        return aqlQueryStreamResult;
    }

}