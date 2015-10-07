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

package org.artifactory.descriptor.security.ldap.group;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The Different LDAP strategies to populate the groups
 *
 * @author Tomer Cohen
 */
@XmlEnum(value = String.class)
public enum LdapGroupPopulatorStrategies {
    @XmlEnumValue("HIERARCHICAL")HIERARCHICAL("DN hierarchy"),
    @XmlEnumValue("STATIC")STATIC("Group contains members"),
    @XmlEnumValue("DYNAMIC")DYNAMIC("Members contain groups");

    private String description;

    LdapGroupPopulatorStrategies(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
