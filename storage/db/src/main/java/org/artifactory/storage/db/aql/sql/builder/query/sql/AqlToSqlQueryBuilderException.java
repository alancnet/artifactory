package org.artifactory.storage.db.aql.sql.builder.query.sql;

import org.artifactory.aql.AqlException;

/**
 * This exception is being thrown in case of failure in the aqlQuery to sqlQuery conversion.
 *
 * @author Gidi Shabat
 */
public class AqlToSqlQueryBuilderException extends AqlException {
    public AqlToSqlQueryBuilderException(String message) {
        super(message);
    }
}
