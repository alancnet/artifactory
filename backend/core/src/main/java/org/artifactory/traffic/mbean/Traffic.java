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

package org.artifactory.traffic.mbean;

import org.artifactory.traffic.TrafficAction;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.TrafficEntry;
import org.artifactory.traffic.entry.TransferEntry;

import java.util.Calendar;
import java.util.List;

/**
 * @author Noam Tenne
 */
public class Traffic implements TrafficMBean {

    private final TrafficService trafficService;

    public Traffic(TrafficService service) {
        trafficService = service;
    }

    @Override
    public long getAccumulatedDownloadSize(long from, long to) {
        List<TrafficEntry> entries = getEntries(from, to);
        return sumTransfer(entries, TrafficAction.DOWNLOAD);
    }

    @Override
    public long getAccumulatedUploadSize(long from, long to) {
        List<TrafficEntry> entries = getEntries(from, to);
        return sumTransfer(entries, TrafficAction.UPLOAD);
    }

    private long sumTransfer(List<TrafficEntry> entries, TrafficAction action) {
        long sum = 0;
        for (TrafficEntry entry : entries) {
            if (action.equals(entry.getAction())) {
                sum += ((TransferEntry) entry).getContentLength();
            }
        }
        return sum;
    }

    private List<TrafficEntry> getEntries(long from, long to) {
        Calendar fromCal = toCalender(from);
        Calendar toCal = toCalender(to);
        return trafficService.getEntryList(fromCal, toCal);
    }

    private Calendar toCalender(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        return cal;
    }
}
