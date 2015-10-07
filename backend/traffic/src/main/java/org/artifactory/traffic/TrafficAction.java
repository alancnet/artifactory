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

package org.artifactory.traffic;

import org.artifactory.traffic.entry.DownloadEntry;
import org.artifactory.traffic.entry.RequestEntry;
import org.artifactory.traffic.entry.TrafficEntry;
import org.artifactory.traffic.entry.UploadEntry;

/**
 * The different traffic action types
 */
public enum TrafficAction {
    REQUEST(RequestEntry.class),
    DOWNLOAD(DownloadEntry.class),
    UPLOAD(UploadEntry.class);

    Class<? extends TrafficEntry> trafficEntryType;
    int numberOfColumns;

    /**
     * Default constructor
     *
     * @param trafficEntryType TrafficEntry class associated with the action type
     */
    TrafficAction(Class<? extends TrafficEntry> trafficEntryType) {
        this.trafficEntryType = trafficEntryType;
    }

    public Class<? extends TrafficEntry> getTrafficEntryType() {
        return trafficEntryType;
    }
}