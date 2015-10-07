package org.artifactory.aql.model;

/**
 * @author Gidi Shabat
 */
public enum AqlItemTypeEnum {
    folder("folder", 0), file("file", 1), any("any", -1);
    public String signature;
    public int type;

    AqlItemTypeEnum(String signature, int type) {
        this.signature = signature;
        this.type = type;
    }

    public static AqlItemTypeEnum fromTypes(int type) {
        for (AqlItemTypeEnum aqlItemTypeEnum : values()) {
            if (aqlItemTypeEnum.type == type) {
                return aqlItemTypeEnum;
            }
        }
        return null;
    }

    public static AqlItemTypeEnum fromSignature(String signature) {
        for (AqlItemTypeEnum aqlItemTypeEnum : values()) {
            if (aqlItemTypeEnum.signature.equals(signature)) {
                return aqlItemTypeEnum;
            }
        }
        return null;
    }
}
