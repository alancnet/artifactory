package org.artifactory.aql.model;

/**
 * @author Gidi Shabat
 */
public class AqlField implements AqlVariable {
    private AqlFieldEnum fieldEnum;

    public AqlField(AqlFieldEnum fieldEnum) {
        this.fieldEnum = fieldEnum;
    }

    public AqlFieldEnum getFieldEnum() {
        return fieldEnum;
    }
}
