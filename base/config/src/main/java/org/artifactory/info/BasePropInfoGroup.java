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
 * The base class of all the information groups
 *
 * @author Noam Tenne
 */
public class BasePropInfoGroup implements PropInfoGroup {

    /**
     * Returns a system property via the property name
     *
     * @param propName The property name
     * @return String - The property value
     */
    public static String getSystemProperty(String propName) {
        if ((propName == null) || ("".equals(propName))) {
            throw new IllegalArgumentException("Property name cannot be empty or null");
        }
        return System.getProperty(propName);
    }

    /**
     * Basic implementation of the getInfo method
     *
     * @return InfoObject[] - An empty info object array
     */
    @Override
    public InfoObject[] getInfo() {
        return new InfoObject[]{};
    }

    /**
     * @return whether this info group should be displayed.
     */
    public boolean isInUse() {
        return true;
    }
}