package org.artifactory.ui.rest.model.artifacts.search.propertysearch;

import java.util.ArrayList;
import java.util.List;

import org.artifactory.descriptor.property.Property;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class PropertyKeyValues extends BaseModel {
    private String key;
    private List<String> values = new ArrayList<>();
    private String propertyType;

    PropertyKeyValues() {
    }

    public PropertyKeyValues(String propertySetName, Property property) {
        updateProps(propertySetName, property);

    }

    /**
     * update props data
     *
     * @param propertySetName - property Set Name
     * @param property        - property instance
     */
    private void updateProps(String propertySetName, Property property) {
        this.key = propertySetName + "." + property.getName();
        property.getPredefinedValues().forEach(preValue -> values.add(preValue.getValue()));
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
}
