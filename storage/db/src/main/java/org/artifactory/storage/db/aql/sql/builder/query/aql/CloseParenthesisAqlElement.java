package org.artifactory.storage.db.aql.sql.builder.query.aql;

/**
 * @author Gidi Shabat
 */
public class CloseParenthesisAqlElement implements AqlQueryElement {
    @Override
    public boolean isOperator() {
        return false;
    }
}
