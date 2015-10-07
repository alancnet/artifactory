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

package org.artifactory.descriptor.repo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Yoav Landman
 */
@XmlEnum(String.class)
public enum SnapshotVersionBehavior {
    @XmlEnumValue("unique")UNIQUE("Unique"),
    @XmlEnumValue("non-unique")NONUNIQUE("Non-unique"),
    @XmlEnumValue("deployer")DEPLOYER("Deployer");

    //The name to display when used in different components
    private String displayName;

    /**
     * Sets the display name of the element
     *
     * @param displayName The display name
     */
    SnapshotVersionBehavior(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of the element
     *
     * @return String - Element display name
     */
    public String getDisplayName() {
        return displayName;
    }
}