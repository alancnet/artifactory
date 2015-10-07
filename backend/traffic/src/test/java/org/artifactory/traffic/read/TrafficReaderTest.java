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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.artifactory.traffic.entry.TrafficEntry;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Tenne
 */
@Test
public class TrafficReaderTest {
    URL trafficLogDir = getClass().getResource("/org/artifactory/traffic/logs");
    TrafficReader trafficReader;

    @BeforeClass
    public void setUp() throws JoranException {
        URL logConfigResource = getClass().getResource("/org/artifactory/traffic/logback.xml");
        File logConfigFile = new File(logConfigResource.getFile());
        Assert.assertNotNull(logConfigFile, "Cannot locate logback configuration file.");
        assertTrue(logConfigFile.exists(), "Cannot locate logback configuration file.");

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.stop();
        configurator.doConfigure(logConfigFile);
        lc.start();
    }

    /**
     * Supply the reader with a null object as the log file directory
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullDir() {
        trafficReader = new TrafficReader(null);
    }

    /**
     * Supply the reader with a file object of a non existing base dir
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNonExistingBaseDir() throws IOException {
        trafficReader = new TrafficReader(new File("mll"));
    }

    /**
     * Supply the reader with null dates
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullDates() throws IOException {
        trafficReader = new TrafficReader(new File(trafficLogDir.getFile()));
        trafficReader.getEntries((Date) null, null);
    }

    /**
     * Supply the reader with invalid dates
     *
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidDates() throws IOException {
        long currentTime = System.currentTimeMillis();
        Date startDate = new Date(currentTime + 1000);
        Date endDate = new Date(currentTime);
        trafficReader = new TrafficReader(new File(trafficLogDir.getFile()));
        trafficReader.getEntries(startDate, endDate);
    }

    /**
     * Test the stream writer with valid in-range dates
     */
    public void testEntriesStream() {
        Date[] dateRange = findDateRange();
        trafficReader = new TrafficReader(new File(trafficLogDir.getFile()));
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];
        long charsWritten = trafficReader.writeFileToStream(new NullOutputStream(), startDate, endDate);
        assertTrue(charsWritten > 0);
    }

    /**
     * Test the reader with valid in-range dates
     */
    public void testEntriesWithinRange() {
        Date[] dateRange = findDateRange();
        trafficReader = new TrafficReader(new File(trafficLogDir.getFile()));
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        long lastEntryDate = 0;
        List<TrafficEntry> entries = trafficReader.getEntries(startDate, endDate);

        assertFalse(entries.isEmpty(), "Entry list shouldn't be empty.");

        for (TrafficEntry currentEntry : entries) {
            long currentEntryDate = currentEntry.getTime();

            if (lastEntryDate > 0) {
                assertFalse(currentEntry.getTime() < lastEntryDate, "Entry list should be sorted.");
            }
            assertTrue(currentEntryDate > startDate.getTime(), "Current entry date should be within range.");
            assertTrue(currentEntryDate < endDate.getTime(), "Current entry date should be within range.");
            lastEntryDate = currentEntryDate;
        }
    }

    /**
     * Test the reader with dates that should be out of range
     *
     * @throws IOException
     */
    public void testOutOfRange() throws IOException {
        trafficReader = new TrafficReader(new File(trafficLogDir.getFile()));
        List<TrafficEntry> trafficEntryList = trafficReader.getEntries(new Date(), new Date());
        assertTrue(trafficEntryList.isEmpty(), "Entry list shouldn't be empty.");
    }

    private Date[] findDateRange() {
        Date startDate = null;
        Date endDate = null;
        String trafficPrefix = "traffic.";
        String logSuffix = ".log";
        Collection<File> logFiles =
                FileUtils.listFiles(new File(trafficLogDir.getFile()), FileFilterUtils.trueFileFilter(),
                        FileFilterUtils.trueFileFilter());

        for (File logFile : logFiles) {
            String logFileName = logFile.getName();
            Assert.assertNotNull(logFileName, "Unable to find dummy log file.");
            assertTrue(logFileName.length() > 0, "Dummy log file name is empty");
            String timeRange =
                    logFileName.substring(trafficPrefix.length(), (logFileName.length() - logSuffix.length()));
            String[] logTimes = timeRange.split("-");
            assertFalse(logTimes.length == 0);
            String logStartTime = logTimes[0];
            Assert.assertNotNull(logStartTime);
            Date logFileStartDate = new Date(Long.parseLong(logStartTime));
            if ((startDate == null) || (logFileStartDate.before(startDate))) {
                startDate = logFileStartDate;
            }
            if ((logTimes.length > 1) && (logTimes[1] != null)) {
                Date logFileEndDate = new Date(Long.parseLong(logTimes[1]));
                if ((endDate == null) || (logFileEndDate.after(endDate))) {
                    endDate = logFileEndDate;
                }
            } else {
                endDate = new Date();
            }
        }

        return new Date[]{startDate, endDate};
    }
}
