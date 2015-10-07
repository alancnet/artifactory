package org.artifactory.sapi.search;

/**
 * Date: 8/6/11
 * Time: 12:13 PM
 *
 * @author Fred Simon
 */
public enum VfsBoolType {
    AND("and"), OR("or");

    public final String str;

    VfsBoolType(String str) {
        this.str = str;
    }
}
