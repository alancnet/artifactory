package org.artifactory.aql;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlValue;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.aql.model.AqlVariableTypeEnum;

/**
 * @author Gidi Shabat
 */
public class AqlFieldResolver {
    public static AqlVariable resolve(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("Cannot accept null or empty property name!");
        }
        AqlFieldEnum value = AqlFieldEnum.value(fieldName);
        if (value != null) {
            return new AqlField(value);
        } else {
            return new AqlValue(AqlVariableTypeEnum.string, fieldName);
        }
    }

    public static AqlField resolve(AqlFieldEnum fieldEnum) {
        if (fieldEnum == null) {
            throw new IllegalArgumentException("Cannot accept null fields!");
        }
        return new AqlField(fieldEnum);
    }

    public static AqlVariable resolve(String value, AqlVariableTypeEnum type) {
        return new AqlValue(type, value);
    }
}
