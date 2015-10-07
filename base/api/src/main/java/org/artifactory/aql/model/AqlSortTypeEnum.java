package org.artifactory.aql.model;

/**
 * @author Gidi Shabat
 */
public enum AqlSortTypeEnum {
    desc("$desc", "DESC"), asc("$asc", "ASC");
    private String aqlName;
    private String sqlName;

    AqlSortTypeEnum(String aqlName, String sqlName) {
        this.aqlName = aqlName;
        this.sqlName = sqlName;
    }

    public String getAqlName() {
        return aqlName;
    }

    public String getSqlName() {
        return sqlName;
    }

    public static AqlSortTypeEnum fromAql(String aql) {
        for (AqlSortTypeEnum sortTypeEnum : values()) {
            if (sortTypeEnum.aqlName.equals(aql)) {
                return sortTypeEnum;
            }
        }
        throw new IllegalStateException("Couldn't find enum with the corresponded aql name");
    }
}
