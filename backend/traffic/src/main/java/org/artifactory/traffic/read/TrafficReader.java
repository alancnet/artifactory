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
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.artifactory.traffic.entry.TrafficEntry;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.artifactory.traffic.TrafficUtils.dateEqualsAfter;
import static org.artifactory.traffic.TrafficUtils.dateEqualsBefore;

/**
 * Traffic entry log file reader
 *
 * @author Noam Tenne
 */
public class TrafficReader {
    public static final String LOG_PREFIX = "traffic.";
    public static final String LOG_SUFFIX = ".log";
    File logDir;

    /**
     * Default constructor
     *
     * @param logDir Directory to search for logs in
     */
    public TrafficReader(File logDir) {
        if ((logDir == null) || !logDir.exists() || !logDir.isDirectory()) {
            throw new IllegalArgumentException("Log directory must be valid.");
        }
        this.logDir = logDir;
    }

    public List<TrafficEntry> getEntries(@Nullable Calendar from, @Nullable Calendar to) {
        //If from is null get all entries from the epoch, if to is null get all entries up to now
        Date fromDate = from != null ? from.getTime() : new Date(0);
        Date toDate = to != null ? to.getTime() : new Date();
        return getEntries(fromDate, toDate);
    }

    /**
     * Returns a list of traffic entries relevant to the given time window - using the traffic stream parser
     *
     * @param startDate Time window start date
     * @param endDate   Time window end date
     * @return List<TrafficEntry> - List of TrafficEntry object relevant to the given time window
     */
    public List<TrafficEntry> getEntries(Date startDate, Date endDate) {
        validateDateRange(startDate, endDate);

        Collection<File> trafficLogs = readFiles(startDate, endDate);

        List<TrafficEntry> entries = new ArrayList<>();
        Reader reader = null;
        for (File trafficLog : trafficLogs) {
            try {
                reader = new InputStreamReader(new FileInputStream(trafficLog), Charsets.UTF_8);
                entries.addAll(TrafficStreamParser.parse(reader, startDate, endDate));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
        Collections.sort(entries);

        return entries;
    }

    /**
     * Writes the entire content of the traffic entry log files which are relevant to the given time window, into the
     * given output stream
     *
     * @param outputStream Stream to write log file contents to
     * @param startDate    Time window start date
     * @param endDate      Time window end date
     * @return long - Total amount of characters written to the output stream
     */
    public long writeFileToStream(OutputStream outputStream, Date startDate, Date endDate) {
        validateDateRange(startDate, endDate);

        if (outputStream == null) {
            throw new IllegalArgumentException("Output stream cannot be null.");
        }

        Collection<File> trafficLogs = readFiles(startDate, endDate);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charsets.UTF_8);
        long totalCharsWritten = 0;

        for (File trafficLog : trafficLogs) {
            Reader fileReader = null;
            try {
                fileReader = new InputStreamReader(new FileInputStream(trafficLog), Charsets.UTF_8);
                int charsCopied = IOUtils.copy(fileReader, writer);
                totalCharsWritten += charsCopied;
                writer.flush();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(fileReader);
            }
        }

        return totalCharsWritten;
    }

    /**
     * Returns a collection of traffic entry log files which are relevant to the given time window
     *
     * @param startDate Time window start date
     * @param endDate   Time window end date
     * @return Collection<File> - Collection of file objects that represent the traffic entry log files which are
     *         relevant to the given time window
     */
    public Collection<File> readFiles(Date startDate, Date endDate) {
        IOFileFilter trafficLogFileFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                String logFileName = file.getName();
                return logFileName.contains(LOG_PREFIX) && logFileName.contains(LOG_SUFFIX);
            }
        };
        Collection<File> collection = FileUtils.listFiles(logDir, trafficLogFileFilter, DirectoryFileFilter.DIRECTORY);
        List<File> trafficLogFiles = Lists.newArrayList(collection);
        Collections.sort(trafficLogFiles);
        List<File> selectedFiles = new ArrayList<>();
        for (File logFile : trafficLogFiles) {
            Date[] logFileDates = getLogFileDates(logFile);

            //Sanity check
            if (logFileDates.length != 2) {
                throw new RuntimeException("Could not read log file dates.");
            }

            //Sanity check
            Date logFileStartDate = logFileDates[0];
            Date logFileEndDate = logFileDates[1];
            if ((logFileStartDate == null) || (logFileEndDate == null)) {
                throw new RuntimeException("Log file dates cannot be null.");
            }

            boolean withinRange = isDateWithinRange(logFileStartDate, logFileEndDate, startDate, endDate);
            if (withinRange) {
                selectedFiles.add(logFile);
            }
        }
        return selectedFiles;
    }

    /**
     * Returns a date array containing the start and end dates of the given log file
     *
     * @param logFile Log file to check
     * @return Date[] - Start and end date of give log file
     */
    private Date[] getLogFileDates(File logFile) {
        //Create default sized date array to return
        Date[] logDates = new Date[2];
        String logFileName = logFile.getName();
        if ((logFileName != null) && (logFileName.length() != 0)) {
            try {
                //Extract the date range from the log file name
                String logTimeRange =
                        logFileName.substring(LOG_PREFIX.length(), (logFileName.length() - LOG_SUFFIX.length()));

                //Split the date range. Array will have only one item, if it is the current active log file
                String[] logTimes = logTimeRange.split("-");

                //Iterate over default date array for using its default size
                for (int i = 0; i < logDates.length; i++) {

                    //If the index does not exist in the time range, insert a default date
                    if (i >= logTimes.length) {
                        logDates[i] = new Date();
                        continue;
                    }
                    logDates[i] = new Date(Long.parseLong(logTimes[i]));
                }
            } catch (IndexOutOfBoundsException ioobe) {
                //If the splitting of the file name has failed, return a default date array
                Date currentTime = new Date();
                return new Date[]{currentTime, currentTime};
            }
        }

        return logDates;
    }

    /**
     * Check if the traffic log file dates are within range of the given time window
     *
     * @param logStartDate Log file start date
     * @param logEndDate   Log file end date
     * @param startDate    Time window start date
     * @param endDate      Time window end date
     * @return boolean - True if log file dates are within time window range. False if not
     */
    private boolean isDateWithinRange(Date logStartDate, Date logEndDate, Date startDate, Date endDate) {
        if ((logStartDate != null) && (logEndDate != null)) {
            /**
             * Scenario: Log file starts after time window starts and ends after time window ends
             *
             * Log start date = 01/03
             * Log end date = 31/03
             * Request start date = 20/02
             * Request end date = 20/03
             */
            if ((dateEqualsBefore(startDate, logStartDate) && dateEqualsAfter(endDate, logStartDate)) ||
                    /**
                     * Scenario: Log file starts before time window starts and ends after time window ends
                     *
                     * Log start date = 01/03
                     * Log end date = 31/03
                     * Request start date = 10/03
                     * Request end date = 20/03
                     */
                    (dateEqualsAfter(startDate, logStartDate)) && (dateEqualsBefore(endDate, logEndDate)) ||
                    /**
                     * Scenario: Log file starts before time window starts and ends after time window starts
                     *
                     * Log start date = 01/03
                     * Log end date = 31/03
                     * Request start date = 10/03
                     */
                    (dateEqualsAfter(startDate, logStartDate) && dateEqualsBefore(startDate, logEndDate)) ||
                    /**
                     * Scenario: Log file starts before time window ends and ends after time window ends
                     *
                     * Log start date = 01/03
                     * Log end date = 31/03
                     * Request end date = 10/03
                     */
                    (dateEqualsAfter(endDate, logStartDate) && dateEqualsBefore(endDate, logEndDate))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks that neither of the given dates are null, and that the given start date's value isn't higher than the
     * given end date's value
     *
     * @param startDate Start date to check
     * @param endDate   End date to check
     */
    private void validateDateRange(Date startDate, Date endDate) {
        if ((startDate == null) || (endDate == null)) {
            throw new IllegalArgumentException("Traffic dates cannot be null.");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("Traffic start date cannot be after end date.");
        }
    }
}
