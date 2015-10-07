package org.artifactory.aql.model;

/**
 * @author Gidi Shabat
 */
public enum AqlComparatorEnum {
    notEquals("$ne"), equals("$eq"), greaterEquals("$gte"), greater("$gt"),
    matches("$match"), notMatches("$nmatch"), lessEquals("$lte"), less("$lt");
    public String signature;

    AqlComparatorEnum(String signature) {
        this.signature = signature;
    }

    public static AqlComparatorEnum value(String comparator) {
        comparator = comparator.toLowerCase();
        for (AqlComparatorEnum comparatorEnum : values()) {
            if (comparatorEnum.signature.equals(comparator)) {
                return comparatorEnum;
            }
        }
        return null;
    }

    public boolean isNegative() {
        return this == notMatches || this == notEquals;

    }
}
