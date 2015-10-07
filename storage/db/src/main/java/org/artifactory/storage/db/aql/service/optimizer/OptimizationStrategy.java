package org.artifactory.storage.db.aql.service.optimizer;

import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public abstract class OptimizationStrategy {

    public void doJob(AqlQuery aqlQuery){
        String trasformation = transformToCharacterRepresentation(aqlQuery);
        optimize(aqlQuery,trasformation);
    }

    public abstract void optimize(AqlQuery aqlQuery, String transformation);

    /**
     * AQL to string transformation function (method)
     * This method transforms the AqlQuery into string representation. The "function" is being used to
     * Detect patterns that can be optimize.
     * <p/>
     * Result
     * Each Aql query element is represented by one character:
     * SimpleCriteria on property key           =             k
     * SimpleCriteria on property value         =             v
     * SimpleCriteria on property key (result)  =             K
     * SimpleCriteria on property value (result)=             V
     * SimpleCriteria on any other field        =             c
     * PropertyCriteria                         =             p
     * OpenParenthesisAqlElement                =             (
     * CloseParenthesisAqlElement               =             )
     * OperatorQueryElement - and               =             a
     * OperatorQueryElement - or                =             o
     * MspAqlElement                            =             m
     * ResultFilterAqlElement                   =             r
     */
    private String transformToCharacterRepresentation(AqlQuery aqlQuery) {
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        StringBuilder builder = new StringBuilder();
        for (AqlQueryElement aqlElement : aqlElements) {
            if (aqlElement instanceof ComplexPropertyCriteria) {
                builder.append("p");
            }
            if (aqlElement instanceof OpenParenthesisAqlElement) {
                builder.append("(");
            }
            if (aqlElement instanceof CloseParenthesisAqlElement) {
                builder.append(")");
            }
            if (aqlElement instanceof ResultFilterAqlElement) {
                builder.append("r");
            }
            if (aqlElement instanceof MspAqlElement) {
                builder.append("m");
            }
            if (aqlElement instanceof SimpleCriteria || aqlElement instanceof SimplePropertyCriteria) {
                AqlField field = (AqlField) ((Criteria) aqlElement).getVariable1();
                SqlTable table1 = ((Criteria) aqlElement).getTable1();
                if (table1.getId() >= 100) {
                    if (AqlFieldEnum.propertyKey == field.getFieldEnum()) {
                        builder.append("k");
                    } else if (AqlFieldEnum.propertyValue == field.getFieldEnum()) {
                        builder.append("v");
                    } else {
                        builder.append("c");
                    }
                } else {
                    if (AqlFieldEnum.propertyKey == field.getFieldEnum()) {
                        builder.append("K");
                    } else if (AqlFieldEnum.propertyValue == field.getFieldEnum()) {
                        builder.append("V");
                    } else {
                        builder.append("C");
                    }
                }
            }
            if (aqlElement instanceof OperatorQueryElement) {
                AqlOperatorEnum operator = ((OperatorQueryElement) aqlElement).getOperatorEnum();
                if (AqlOperatorEnum.and == operator) {
                    builder.append("a");
                } else {
                    builder.append("o");
                }
            }
        }
        return builder.toString();
    }
}
