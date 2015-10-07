package org.artifactory.aql.model;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.AqlParserException;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Gidi Shabat
 */
public class AqlValue implements AqlVariable {

    private AqlVariableTypeEnum valueType;
    private String value;

    public AqlValue(AqlVariableTypeEnum valueType, String value) {
        this.valueType = valueType;
        this.value = value;
    }

    public Object toObject() throws AqlException {
        Object result = value;
        if (value == null) {
            return null;
        }
        if (AqlVariableTypeEnum.string == valueType) {
            result = value;
        }
        if (AqlVariableTypeEnum.date == valueType) {
            try {
                result = ISODateTimeFormat.dateOptionalTimeParser().parseMillis(value);
            } catch (Exception e) {
                throw new AqlParserException(
                        String.format("Invalid Date format: %s, AQL expect ISODateTimeFormat", value), e);
            }
        }
        if (AqlVariableTypeEnum.longInt == valueType) {
            try {
                result = Long.valueOf(value);
            } catch (NumberFormatException e) {
                throw new AqlException("AQL Expect long value but found:" + value.toString() + "\n");
            }
        }
        if (AqlVariableTypeEnum.itemType == valueType) {
            AqlItemTypeEnum aqlItemTypeEnum = AqlItemTypeEnum.fromSignature(value);
            if(aqlItemTypeEnum !=null){
                result = aqlItemTypeEnum.type;
            }else{
                throw new AqlException(String.format("Invalid file type: %s, valid types are : %s, %s, %s", value,
                        AqlItemTypeEnum.file.signature, AqlItemTypeEnum.folder.signature,
                        AqlItemTypeEnum.any.signature));
            }
        }
        if (AqlVariableTypeEnum.integer == valueType) {
            try {
                result = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new AqlException("AQL Expect integer value but found:" + value.toString() + "\n");
            }
        }
        return result;

    }
}
