package org.artifactory.storage.db.aql.sql.builder.query.aql;

/**
 * Represent propery criteria which is actually two criterias:
 * "_property_key" "$equals" "<some_key>" "and" "_property_value" "$equals" "<some_value>"
 *
 * @author Gidi Shabat
 */
public class MspAqlElement implements AqlQueryElement {
    private int tableId;

    public MspAqlElement(int tableId) {
        this.tableId = tableId;
    }

    public int getTableId() {
        return tableId;
    }

    @Override
    public boolean isOperator() {
        return false;
    }
}
