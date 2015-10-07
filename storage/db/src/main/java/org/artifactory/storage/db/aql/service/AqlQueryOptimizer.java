package org.artifactory.storage.db.aql.service;

import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.aql.service.optimizer.FileTypeOptimization;
import org.artifactory.storage.db.aql.service.optimizer.PropertyCriteriaRelatedWithOr;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;

/**
 * @author Gidi Shabat
 */
public class AqlQueryOptimizer {
    private QueryOptimizer optimizer;

    public AqlQueryOptimizer(DbType dbType) {
        // Since the optimisation in each database type is different we need to get the database type and accordingly init it relevant optimizations
        optimizer = loadOptimizerForDatabase(dbType);
    }

    private QueryOptimizer loadOptimizerForDatabase(DbType dbType) {
        switch (dbType) {
            case DERBY: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case MYSQL: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case ORACLE: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case MSSQL: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
            case POSTGRESQL: {
                return new QueryOptimizer(
                        new FileTypeOptimization(),
                        new PropertyCriteriaRelatedWithOr()
                );
            }
        }
        throw new RuntimeException("Unsupported database type" + dbType.name());
    }


    public void optimize(AqlQuery aqlQuery) {
        optimizer.optimize(aqlQuery);
    }
}
