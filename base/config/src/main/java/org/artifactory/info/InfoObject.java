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

package org.artifactory.info;

/**
 * An Object to hold system information in form of key and value
 *
 * @author Noam Tenne
 */
public class InfoObject {

    /**
     * Key
     */
    private String propertyName;
    /**
     * Value
     */
    private String propertyValue;

    /**
     * Main constructor
     *
     * @param propertyName  The key
     * @param propertyValue The value
     */
    public InfoObject(String propertyName, String propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    /**
     * Returns the key
     *
     * @return String - key
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the value
     *
     * @return String - value
     */
    public String getPropertyValue() {
        return propertyValue;
    }
}
