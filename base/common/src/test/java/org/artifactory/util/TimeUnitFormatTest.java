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

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the {@link TimeUnitFormat} class.
 *
 * @author Yossi Shaul
 */
@Test
public class TimeUnitFormatTest {

    public void stillNanos() {
        assertEquals(TimeUnitFormat.getTimeString(2345), "2345 nanos");
        assertEquals(TimeUnitFormat.getTimeString(999_999), "999999 nanos");
    }

    public void toMillis() {
        assertEquals(TimeUnitFormat.getTimeString(TimeUnit.MILLISECONDS.toNanos(1)), "1 millis");
        assertEquals(TimeUnitFormat.getTimeString(1_100_000), "1.1 millis");
    }

    public void toSeconds() {
        assertEquals(TimeUnitFormat.getTimeString(TimeUnit.SECONDS.toNanos(1)), "1 secs");
        assertEquals(TimeUnitFormat.getTimeString(9_755_461_234L), "9.76 secs");
    }

    public void toMinutes() {
        assertEquals(TimeUnitFormat.getTimeString(TimeUnit.MINUTES.toNanos(1)), "1 minutes");
        assertEquals(TimeUnitFormat.getTimeString(1_456_000L * 1000 * 60 * 10), "14.56 minutes");
    }

    public void toSecondsWithMillisecondTimeUnit() {
        assertEquals(TimeUnitFormat.getTimeString(1000, TimeUnit.MILLISECONDS), "1 secs");
        assertEquals(TimeUnitFormat.getTimeString(TimeUnit.NANOSECONDS.toMillis(9_755_461_234L),
                TimeUnit.MILLISECONDS), "9.76 secs");
    }

}
