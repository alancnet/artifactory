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

package org.artifactory.common.property;

import org.artifactory.log.BootstrapLogger;

/**
 * Maps the value of a property from minutes to its counterpart in seconds
 *
 * @author Tomer Cohen
 */
public class MinutesToSecondsPropertyMapper extends PropertyMapperBase {

    public MinutesToSecondsPropertyMapper(String origPropertyName) {
        super(origPropertyName);
    }

    @Override
    public String map(String origValue) {
        int valueInMinutes;
        try {
            valueInMinutes = Integer.parseInt(origValue);
        } catch (NumberFormatException e) {
            String msg = "'" + origValue + "' is an illegal value.";
            BootstrapLogger.error(msg);
            throw new IllegalArgumentException(msg, e);
        }
        return String.valueOf(valueInMinutes * 60);
    }
}
