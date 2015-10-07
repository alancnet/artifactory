package org.artifactory.ui.rest.model.admin.configuration.propertysets;

import java.util.List;
import java.util.stream.Collectors;

import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.google.common.collect.Lists;

/**
 * Model for the admin's Property Set dialog
 *
 * @author Dan Feldman
 */
@JsonIgnoreProperties({"visible"})
public class AdminPropertySetModel implements RestModel {

    String name;
    List<AdminPropertiesModel> properties = Lists.newArrayList();
    Boolean visible;

    public AdminPropertySetModel() {
    }

    public AdminPropertySetModel(PropertySet propertySet) {
        this.name = propertySet.getName();
        this.visible = propertySet.isVisible();
        this.properties = propertySet.getProperties().stream()
                .map(AdminPropertiesModel::new)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public PropertySet getPropertySetFromModel() {
        PropertySet fromModel = new PropertySet();
        fromModel.setName(name);
        fromModel.setProperties(properties.stream()
                .map(AdminPropertiesModel::toProperty)
                .collect(Collectors.toList()));
        return fromModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AdminPropertiesModel> getProperties() {
        return properties;
    }

    public void setProperties(List<AdminPropertiesModel> properties) {
        this.properties = properties;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdminPropertySetModel)) return false;

        AdminPropertySetModel that = (AdminPropertySetModel) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getProperties() != null ? !getProperties().equals(that.getProperties()) : that.getProperties() != null)
            return false;
        return !(getVisible() != null ? !getVisible().equals(that.getVisible()) : that.getVisible() != null);

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getProperties() != null ? getProperties().hashCode() : 0);
        result = 31 * result + (getVisible() != null ? getVisible().hashCode() : 0);
        return result;
    }

    /**
     * Serialization
     */
    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
