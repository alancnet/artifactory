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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.input.NullReader;
import org.apache.commons.lang.time.DateUtils;
import org.artifactory.traffic.entry.TrafficEntry;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Unit test for the TrafficStreamParser
 *
 * @author Noam Tenne
 */
@Test
public class TrafficStreamParserTest {
    SimpleDateFormat entryDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    URL trafficLogFile = getClass().getResource("/org/artifactory/traffic/logs/traffic.1238313542120.log");

    /**
     * Supply the parser with a null object instead of a reader
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullReader() throws IOException {
        TrafficStreamParser.parse(null, null, null);
    }

    /**
     * Supply the parser with null objects instead of dates
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullDates() throws IOException {
        TrafficStreamParser.parse(new NullReader(0), null, null);
    }

    /**
     * Supply the parser with invalid dates
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidDates() throws IOException {
        long currentTime = System.currentTimeMillis();
        Date startDate = new Date(currentTime + 1000);
        Date endDate = new Date(currentTime);
        TrafficStreamParser.parse(new NullReader(0), startDate, endDate);
    }

    /**
     * Test the parser with valid in-range dates
     *
     * @throws IOException
     * @throws ParseException
     */
    public void testWithinRange() throws IOException, ParseException {
        List<String> list = Resources.readLines(trafficLogFile, Charsets.UTF_8);
        String firstEntry = list.get(0);
        String[] splitFirstEntry = firstEntry.split("\\|");
        String lastEntry = list.get(list.size() - 1);
        String[] splitLastEntry = lastEntry.split("\\|");

        Date startDate = entryDateFormat.parse(splitFirstEntry[0]);
        Date endDate = entryDateFormat.parse(splitLastEntry[0]);
        startDate = DateUtils.truncate(startDate, Calendar.MINUTE);
        endDate = DateUtils.round(endDate, Calendar.MINUTE);
        List<TrafficEntry> entries =
                TrafficStreamParser.parse(new FileReader(trafficLogFile.getFile()), startDate, endDate);

        Assert.assertFalse(entries.isEmpty(), "Log parsing should return results.");
        for (TrafficEntry entry : entries) {
            long currentEntryDate = entry.getTime();
            assertTrue(currentEntryDate > startDate.getTime(), "Current entry date should be within range.");
            assertTrue(currentEntryDate < endDate.getTime(), "Current entry date should be within range.");
        }
    }

    /**
     * Test the parser with dates that should be out of range
     *
     * @throws IOException
     */
    public void testOutOfRange() throws IOException {
        List<TrafficEntry> trafficEntryList =
                TrafficStreamParser.parse(new FileReader(trafficLogFile.getFile()), new Date(), new Date());
        assertTrue(trafficEntryList.isEmpty(), "Log parsing shouldn't return results.");
    }
}