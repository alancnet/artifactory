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

package org.artifactory.traffic.read;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.traffic.entry.TokenizedTrafficEntryFactory;
import org.artifactory.traffic.entry.TrafficEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Textual traffic entry parser
 *
 * @author Noam Tenne
 */
public abstract class TrafficStreamParser {
    private TrafficStreamParser() {
        // utility class
    }

    /**
     * Parses a stream of textual traffic entries, converts them to traffic entry objects, and returns a list of the
     * objects that are relevant to the given time window
     *
     * @param entries   Stream of textual entries
     * @param startDate Time window start date
     * @param endDate   Time window end date
     * @return List<TrafficEntry> - List of TrafficEntry object relevant to the given time window
     * @throws IOException
     */
    public static List<TrafficEntry> parse(Reader entries, Date startDate, Date endDate) throws IOException {
        //Perform some sanity checks
        if (entries == null) {
            throw new IllegalArgumentException("Entry reader cannot be null");
        }
        if ((startDate == null) || (endDate == null)) {
            throw new IllegalArgumentException("Traffic dates cannot be null.");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Traffic start date cannot be after end date.");
        }

        BufferedReader source = null;
        try {
            source = new BufferedReader(entries);
            List<TrafficEntry> entryList = new ArrayList<>();
            String entryRow;
            while ((entryRow = source.readLine()) != null) {
                if (StringUtils.isNotBlank(entryRow)) {
                    TrafficEntry trafficEntry = TokenizedTrafficEntryFactory.newTrafficEntry(entryRow);
                    if (isWithinDateRange(trafficEntry, startDate, endDate)) {
                        entryList.add(trafficEntry);
                    } else if (trafficEntry.getTime() >= endDate.getTime()) {
                        // file entries are sorted, once we reach an entry after the endDate we can stop parsing
                        break;
                    }
                }
            }
            return entryList;
        } finally {
            IOUtils.closeQuietly(entries);
            IOUtils.closeQuietly(source);
        }
    }

    /**
     * Checks if the given traffic entry is relevant to the given time window
     *
     * @param baseEntry Entry to check
     * @param startDate Time window start date
     * @param endDate   Time window end date
     * @return boolean - True if the given entry is relevant to the given time window. False if not
     */
    private static boolean isWithinDateRange(TrafficEntry baseEntry, Date startDate, Date endDate) {
        long entryTime = baseEntry.getTime();
        return startDate.getTime() <= entryTime && entryTime < endDate.getTime();
    }
}
