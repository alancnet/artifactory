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

import javax.xml.bind.annotation.XmlType;

/**
 * @author Yoav Landman
 */
@XmlType(name = "PredefinedValueType", propOrder = {"value", "defaultValue"}, namespace = Descriptor.NS)
public class PredefinedValue implements Descriptor {
    private String value;
    private boolean defaultValue;

    public PredefinedValue() {
    }

    public PredefinedValue(String value, boolean defaultValue) {
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PredefinedValue)) {
            return false;
        }

        PredefinedValue value1 = (PredefinedValue) o;

        if (value != null ? !value.equals(value1.value) : value1.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return value;
    }
}