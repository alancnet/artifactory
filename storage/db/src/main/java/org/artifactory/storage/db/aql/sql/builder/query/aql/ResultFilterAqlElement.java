package org.artifactory.storage.db.aql.sql.builder.query.aql;

/**
 * @author Gidi Shabat
 */
public class ResultFilterAqlElement implements AqlQueryElement {

    @Override
    public boolean isOperator() {
        return false;
    }
}
