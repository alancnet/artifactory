package org.artifactory.storage.db.aql.service;

import org.artifactory.storage.db.aql.service.optimizer.OptimizationStrategy;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;

/**
 * @author Gidi Shabat
 */
public class QueryOptimizer {
    private OptimizationStrategy[] strategies;

    public QueryOptimizer(OptimizationStrategy... strategies) {
        this.strategies = strategies;
    }

    public void optimize(AqlQuery aqlQuery) {
        for (OptimizationStrategy strategy : strategies) {
            strategy.doJob(aqlQuery);
        }
    }
}
