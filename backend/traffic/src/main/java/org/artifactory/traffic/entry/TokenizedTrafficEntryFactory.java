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

package org.artifactory.traffic.entry;

import org.apache.commons.lang.StringUtils;
import org.artifactory.traffic.TrafficAction;

import java.lang.reflect.Constructor;

/**
 * @author Yoav Landman
 */
public abstract class TokenizedTrafficEntryFactory {
    private TokenizedTrafficEntryFactory() {
        // utility class
    }

    /**
     * Initializes a new traffic entry class using a textual entry
     *
     * @param entry Textual entry to create into an object
     * @return TrafficEntry - Object created from textual entry
     */
    public static TrafficEntry newTrafficEntry(String entry) {
        if (StringUtils.isEmpty(entry)) {
            throw new IllegalArgumentException("Entry is empty");
        }

        String[] entryElements = StringUtils.split(entry, TokenizedTrafficEntry.COLUMN_SEPARATOR);
        if (entryElements.length < 3) {
            throw new IllegalArgumentException("Entry should contain at least three elements: " + entry);
        }

        //Action specification should always be in the third column
        String action = entryElements[2];
        TrafficAction trafficAction = TrafficAction.valueOf(action);
        try {
            Constructor<? extends TrafficEntry> constructor =
                    trafficAction.getTrafficEntryType().getConstructor(String.class);
            //Will init the date and duration
            return constructor.newInstance(entry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
