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

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for all information groups that use system properties
 *
 * @author Noam Tenne
 */
public class SystemPropInfoGroup extends BasePropInfoGroup {

    /**
     * Property names
     */
    private String[] properties;

    /**
     * Main constructor
     *
     * @param properties A collection of property names
     */
    public SystemPropInfoGroup(String... properties) {
        this.properties = properties;
    }

    public SystemPropInfoGroup() {
    }

    /**
     * Receives property names and sets them in the global variable
     *
     * @param properties
     */
    public void setProperties(String... properties) {
        this.properties = properties;
    }

    /**
     * Returns all the info objects from the current group
     *
     * @return InfoObject[] - Collection of info objects from current group
     */
    @Override
    public InfoObject[] getInfo() {
        List<InfoObject> infoList = new ArrayList<>();

        for (String prop : properties) {
            String value = getSystemProperty(prop);
            if (value != null) {
                InfoObject infoObject = new InfoObject(prop, value);
                infoList.add(infoObject);
            }
        }

        return infoList.toArray(new InfoObject[infoList.size()]);
    }
}