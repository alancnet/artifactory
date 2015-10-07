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

import org.artifactory.traffic.TrafficAction;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The main implementation of the TrafficEntry interface
 *
 * @author Noam Tenne
 * @author Yoav Landman
 */
public abstract class TrafficEntryBase implements TrafficEntry {
    protected static final DateTimeFormatter ENTRY_DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    protected long time;
    protected long duration;
    protected AtomicReference<String> formattedDate = new AtomicReference<>();

    /**
     * Default constructor
     */
    protected TrafficEntryBase() {
        this.time = System.currentTimeMillis();
        this.duration = 0;
    }

    protected TrafficEntryBase(long duration) {
        this.time = System.currentTimeMillis();
        this.duration = duration;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public long getDuration() {
        return this.duration;
    }

    @Override
    public abstract TrafficAction getAction();

    /**
     * Compares the entry by date
     *
     * @param that Entry to compare to
     * @return int - Date comparison result
     */
    @Override
    public int compareTo(TrafficEntry that) {
        return Long.compare(time, that.getTime());
    }

    /**
     * Formats the entry date (according to the textual entry format) and returns it
     *
     * @return String - Textual entry type formatted date
     */
    String getFormattedDate() {
        //Lazy initialize if null
        formattedDate.compareAndSet(null, ENTRY_DATE_FORMATTER.print(time));
        return formattedDate.get();
    }
}