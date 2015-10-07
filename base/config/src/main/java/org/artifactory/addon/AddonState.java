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

package org.artifactory.addon;

/**
 * The states that an installed addon can be in
 *
 * @author Noam Y. Tenne
 */
public enum AddonState {
    ACTIVATED("Activated"),
    DISABLED("Disabled"),
    INACTIVATED("Inactivated"),
    NOT_CONFIGURED("Not Configured"),
    NOT_LICENSED("Not Licensed");

    private String name;

    /**
     * Main constructor
     *
     * @param name State name
     */
    AddonState(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the state
     *
     * @return State name
     */
    public String getName() {
        return name;
    }
}
