package org.artifactory.ui.rest.model.admin.configuration.propertysets;

import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Dan Feldman
 */
public class PropertySetNameModel extends BaseModel {

    String name;
    Integer propertiesCount;

    public PropertySetNameModel() {

    }

    public PropertySetNameModel(String name, int propertiesCount) {
        this.name = name;
        this.propertiesCount = propertiesCount;
    }

    public PropertySetNameModel(PropertySet propertySet) {
        this.name = propertySet.getName();
        this.propertiesCount = propertySet.getProperties().size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPropertiesCount() {
        return propertiesCount;
    }

    public void setPropertiesCount(Integer count) {
        this.propertiesCount = count;
    }
}
