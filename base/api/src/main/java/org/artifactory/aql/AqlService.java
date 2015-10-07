package org.artifactory.aql;

import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.AqlLazyResult;
import org.artifactory.aql.result.rows.AqlRowResult;

/**
 * @author Gidi Shabat
 */
public interface AqlService {

    /**
     * Parse the AQL query,
     * convert the parser result into Aql query,
     * convert the Aql query to sql query
     * and finally execute the query lazy
     */
    AqlLazyResult executeQueryLazy(String query);

    /**
     * Parse the AQL query,
     * convert the parser result into AqlApi query,
     * convert the AqlApi query to sql query
     * and finally execute the query eagerly
     */
    AqlEagerResult executeQueryEager(String query);

    /**
     * Converts the AQL API QUERY into aqlApi query,
     * then convert the aqlApi query into SQL query,
     * and finally execute the query eagerly
     */

    <T extends AqlRowResult> AqlEagerResult<T> executeQueryEager(AqlBase<? extends AqlBase, T> aqlBase);

    AqlLazyResult executeQueryLazy(AqlBase query);

}
