package org.artifactory.sapi.search;

/**
 * Date: 8/5/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public enum VfsComparatorType {
    ANY("IS NOT NULL"), IN("IN"), NONE("IS NULL"),
    EQUAL("="), NOT_EQUAL("!="),
    GREATER_THAN(">"), LOWER_THAN("<"),
    GREATER_THAN_EQUAL(">="), LOWER_THAN_EQUAL("<="),
    CONTAINS("LIKE"), NOT_CONTAINS("NOT LIKE");

    public final String str;

    VfsComparatorType(String str) {
        this.str = str;
    }

    public boolean acceptValue() {
        return this != ANY && this != NONE;
    }

    public boolean acceptFunction() {
        return acceptValue() && this != CONTAINS && this != IN;
    }
}
