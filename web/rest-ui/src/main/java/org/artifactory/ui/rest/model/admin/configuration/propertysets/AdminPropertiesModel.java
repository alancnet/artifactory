package org.artifactory.ui.rest.model.admin.configuration.propertysets;

import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertyType;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Dan Feldman
 */
@JsonIgnoreProperties({"closedPredefinedValues", "multipleChoice", "getFormattedValues", "valueCount"})
public class AdminPropertiesModel extends Property implements RestModel {

    public PropertyType propertyType; //Will be null - gets populated during serialization chain

    public AdminPropertiesModel() {
    }

    public AdminPropertiesModel(Property that) {
        this.setName(that.getName());
        this.setPredefinedValues(that.getPredefinedValues());
        this.setPropertyType(that.getPropertyType().toString());
    }

    public Property toProperty() {
        return this;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
