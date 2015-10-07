/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.descriptor.property;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.util.AlreadyExistsException;
import org.artifactory.util.DoesNotExistException;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yoav Landman
 */
@XmlType(name = "PropertyType", propOrder = {"name", "closedPredefinedValues", "multipleChoice", "predefinedValues"},
        namespace = Descriptor.NS)
public class Property implements Descriptor {
    private String name;
    private boolean closedPredefinedValues;
    private boolean multipleChoice;
    //private String propertyType;

    @XmlElementWrapper(name = "predefinedValues")
    @XmlElement(name = "predefinedValue", required = false)
    private List<PredefinedValue> predefinedValues = new ArrayList<>();

    public Property() {
    }

    public Property(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClosedPredefinedValues() {
        return closedPredefinedValues;
    }

    public void setClosedPredefinedValues(boolean closedPredefinedValues) {
        this.closedPredefinedValues = closedPredefinedValues;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public List<PredefinedValue> getPredefinedValues() {
        return predefinedValues;
    }

    @JsonIgnore
    public String getFormattedValues() {
        StringBuilder builder = new StringBuilder();
        for (PredefinedValue predefinedValue : predefinedValues) {
            builder.append(", \"").append(predefinedValue.getValue()).append("\"");
            if (predefinedValue.isDefaultValue()) {
                builder.append(" (default)");
            }
        }

        if (builder.length() == 0) {
            return "";
        }
        return builder.substring(2);
    }

    public void setPredefinedValues(List<PredefinedValue> predefinedValues) {
        this.predefinedValues = predefinedValues;
    }

    private PredefinedValue getPredefinedValue(String value) {
        for (PredefinedValue predefinedValue : predefinedValues) {
            if (predefinedValue.getValue().equals(value)) {
                return predefinedValue;
            }
        }

        return null;
    }

    public void updatePredefinedValue(PredefinedValue predefinedValue) {
        int index = predefinedValues.indexOf(predefinedValue);
        if (index == -1) {
            throw new DoesNotExistException("Predefined Value " + predefinedValue.getValue() + " does not exist");
        }
        predefinedValues.set(index, predefinedValue);
    }

    public boolean isPredefinedValueExists(String value) {
        return getPredefinedValue(value) != null;
    }

    public int getValueCount() {
        return predefinedValues.size();
    }

    public void addPredefinedValue(PredefinedValue predefinedValue) {
        String predefinedValueName = predefinedValue.getValue();
        if (isPredefinedValueExists(predefinedValueName)) {
            throw new AlreadyExistsException("Predefined Value " + predefinedValueName + " already exists");
        }
        predefinedValues.add(predefinedValue);
    }

    public PredefinedValue removePredefinedValue(String value) {
        PredefinedValue predefinedValue = getPredefinedValue(value);
        if (predefinedValue == null) {
            return null;
        }

        //Remove the property set from the property sets list
        predefinedValues.remove(predefinedValue);

        return predefinedValue;
    }

    @XmlTransient
    public PropertyType getPropertyType() {
        if (!isClosedPredefinedValues()) {
            return PropertyType.ANY_VALUE;
        }
        if (isMultipleChoice()) {
            return PropertyType.MULTI_SELECT;
        }
        return PropertyType.SINGLE_SELECT;
    }

    public void setPropertyType(String propertyType) {
        switch (propertyType) {
            case "ANY_VALUE": {
                closedPredefinedValues = false;
                multipleChoice = false;
                break;
            }
            case "MULTI_SELECT": {
                closedPredefinedValues = true;
                multipleChoice = true;
                break;
            }
            case "SINGLE_SELECT": {
                closedPredefinedValues = true;
                multipleChoice = false;
                break;
            }
            default: {
                closedPredefinedValues = false;
                multipleChoice = false;
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Property)) {
            return false;
        }

        Property property = (Property) o;

        return !(name != null ? !name.equals(property.name) : property.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}