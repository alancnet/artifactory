/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Formats time units to human readable strings.
 *
 * @author Yossi Shaul
 */
public abstract class TimeUnitFormat {

    private static final long NANOS_IN_ONE_MILLIS = 1_000_000L;
    private static final long NANOS_IN_ONE_SECOND = NANOS_IN_ONE_MILLIS * 1000;
    private static final long NANOS_IN_ONE_MINUTE = NANOS_IN_ONE_SECOND * 60;

    /**
     * @param duration Duration in the time unit specified with the second parameter
     * @return A formatted string with the closest matching time unit
     */
    public static String getTimeString(long duration, TimeUnit timeUnit) {
        return getTimeString(timeUnit.toNanos(duration));
    }

    /**
     * @param nanos Time in nano seconds
     * @return A formatted string with the closest matching time unit
     */
    public static String getTimeString(long nanos) {
        // use a thread safe alternative or thread local in the future
        NumberFormat numberFormat = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.UK));

        if (nanos < NANOS_IN_ONE_MILLIS) {
            // show in nanoseconds
            return nanos + " nanos";
        } else if (nanos < NANOS_IN_ONE_SECOND) {
            // show in millis
            return numberFormat.format((float) nanos / NANOS_IN_ONE_MILLIS) + " millis";
        } else if (nanos < NANOS_IN_ONE_MINUTE) {
            // show in seconds
            return numberFormat.format((float) nanos / NANOS_IN_ONE_SECOND) + " secs";
        } else {
            // show in minutes
            return numberFormat.format((float) nanos / NANOS_IN_ONE_MINUTE) + " minutes";
        }
    }
}
