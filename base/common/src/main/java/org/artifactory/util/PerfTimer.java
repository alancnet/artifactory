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

package org.artifactory.util;

/**
 * Performance monitor.
 *
 * @author Yossi Shaul
 */
public class PerfTimer {

    private final long start;
    private long end;

    /**
     * Creates the performance timer and records the start time
     */
    public PerfTimer() {
        start = System.nanoTime();
    }

    /**
     * Stops the timer and returns the execution time in nanos.
     *
     * @return Execution time in nanos
     */
    public long stop() {
        end = System.nanoTime();
        return end - start;
    }

    /**
     * @return Execution time in nanos since the start time
     */
    public long getTime() {
        if (end < start) {
            return -1;  // illegal state but we don't want to fail application code
        }
        return end - start;
    }

    /**
     * @return The time passed since the start time (regardless of the stop time)
     */
    public String currentTimeString() {
        return getTimeString(System.nanoTime() - start);
    }

    /**
     * @return A human readable string adjusted by time units
     */
    public String getTimeString() {
        long nanos = getTime();
        return getTimeString(nanos);
    }

    @Override
    public String toString() {
        return getTimeString();
    }

    private String getTimeString(long nanos) {
        return TimeUnitFormat.getTimeString(nanos);
    }

}
