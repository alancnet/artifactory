package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

/**
 * This enum represent the supported table join types.
 *
 * @author Gidi Shabat
 */
public enum AqlJoinTypeEnum {
    innerJoin("inner join"),leftOuterJoin("left outer join");
    public String signature;

    AqlJoinTypeEnum(String signature) {
        this.signature = signature;
    }
}
