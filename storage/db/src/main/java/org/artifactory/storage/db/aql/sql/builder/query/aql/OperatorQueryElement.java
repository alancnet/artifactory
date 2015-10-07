package org.artifactory.storage.db.aql.sql.builder.query.aql;

import org.artifactory.aql.model.AqlOperatorEnum;

/**
 * @author Gidi Shabat
 */
public class OperatorQueryElement implements AqlQueryElement {

    private final AqlOperatorEnum operatorEnum;

    public OperatorQueryElement(AqlOperatorEnum operatorEnum) {
        this.operatorEnum = operatorEnum;
    }

    public AqlOperatorEnum getOperatorEnum() {
        return operatorEnum;
    }

    @Override
    public boolean isOperator() {
        return true;
    }
}
