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

package org.artifactory.traffic.policy;

import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.rolling.helper.RenameUtil;

import java.io.File;

/**
 * Overrides the TimeBasedRollingPolicy, to enable the addition of the start and end times of each log file, to their
 * name.
 * <p/>
 * When starting up the logger for the first time, we take out the base log file name from the XML:<br>
 * $ARTIFACTORY_HOME/logs/traffic<br> We then check to see if the startedTime field is set (shouldn't be), and set it as
 * the current time.<br> We override the ch.qos.logback.core.rolling.TimeBasedRollingPolicy#getActiveFileName() method,
 * so it will return the base file name (traffic), after appending to it the startedTime and the log suffix, resulting
 * in:<br> $ARTIFACTORY_HOME/logs/traffic.SOMETIME_LONG.log<br> </P>
 * <p/>
 * When a rollover should occur, the overriden ch.qos.logback.core.rolling.TimeBasedRollingPolicy#rollover() method,
 * keeps aside the content of the startedTime field, sets it's new value to the current time, and the renames the
 * current active log file to:<br> $ARTIFACTORY_HOME/logs/traffic.OLD_START_TIME_LONG-NEW_START_TIME_LONG.log<br> After
 * several rollovers, these actions shold result in a list of logs as such:<br> <UL>
 * <LI>$ARTIFACTORY_HOME/logs/traffic.SOMETIME_LONG-SOMETIME_LONG.log</LI> <LI>$ARTIFACTORY_HOME/logs/traffic.SOMETIME_LONG-SOMETIME_LONG.log</LI>
 * <LI>$ARTIFACTORY_HOME/logs/traffic.SOMETIME_LONG-SOMETIME_LONG.log</LI> <LI>$ARTIFACTORY_HOME/logs/traffic.SOMETIME_LONG.log</LI>
 * </UL> </P>
 * <p/>
 * If the logger will be stopped, and started again, the overriden ch.qos.logback.core.rolling.TimeBasedRollingPolicy#start()
 * method, will first search the log dir for a last active log file (traffic.SOMETIME_LONG.log), take it's time value
 * from the name, and set it in the startedTime field, So it will continue the last log that was started, but not rolled
 * over.<br> In a case when a last active log file will not be found, it will create a new one, with the current time,
 * at the startedTime field. </P>
 *
 * @author Noam Tenne
 */
public class TrafficTimeBasedRollingPolicy extends TimeBasedRollingPolicy {

    /**
     * Log suffix
     */
    private static final String LOG_EXT = ".log";

    /**
     * Date seperator
     */
    private static final String TS_SEP = "-";

    /**
     * Start time of the currently active log. Accessible by tests
     */
    long startedTime;

    /**
     * Create a local instance of util, since it is private in the super class
     */
    RenameUtil util = new RenameUtil();

    /**
     * Path of currently active log file
     */
    String activeLogFilePath;

    @Override
    public void start() {
        super.start();

        //Get active log file from XML, if we don't already have it
        if (activeLogFilePath == null) {
            activeLogFilePath = getParentsRawFileProperty();
        }

        //Sanity check
        if (activeLogFilePath == null) {
            throw new IllegalStateException("Appender file name must be set.");
        }

        //Set the LR for our utility object
        util.setContext(this.context);

        //If we do not have the log start date
        if (startedTime == 0) {

            //Search the directory of the active log file, to see if we can locate the last active one (and continue it)
            File logsDir = new File(activeLogFilePath).getParentFile();
            final File[] logDirContent = logsDir.listFiles();
            for (File currentlyCheckedFile : logDirContent) {
                if (findLastActiveLogFile(currentlyCheckedFile)) {
                    return;
                }
            }

            //If last log file is not found, start it off with the current time
            startedTime = System.currentTimeMillis();
        }
    }

    /**
     * Finds and sets the correct log start time in the last active log file. Searches for the last active log file
     * which hasn't been rolled over, and if it will be found, the timestamp Will be taken and set as the current log
     * file start time
     *
     * @param currentlyCheckedFile File to validate as a last active traffic log, to find the correct start time
     * @return boolean - True if last active log file found, false it not.
     */
    private boolean findLastActiveLogFile(File currentlyCheckedFile) {
        if (currentlyCheckedFile != null) {
            String currentFileName = currentlyCheckedFile.getName();
            String activeLogFileName = new File(activeLogFilePath).getName();

            //Sanity checks
            if ((currentFileName.length() == 0) || (activeLogFileName.length() == 0)) {
                return false;
            }

            /**
             * -If the currently checked file is actually a file.
             * -If it begins with the same path and prefix as the XML specified active file (means it's a traffic log).
             * -If it does not contain the date seperator '-' (means it's a log file that hasn't rolled over yet.
             * -If it ends with the log extension (*.log)
             */
            if (currentlyCheckedFile.isFile() && currentFileName.startsWith(activeLogFileName) &&
                    !currentFileName.contains(TS_SEP) && currentFileName.endsWith(LOG_EXT)) {
                try {
                    //Strip the start-time stamp from the last active log file
                    String startedStr = currentFileName
                            .substring(activeLogFileName.length() + 1, currentFileName.length() - LOG_EXT.length());

                    //Set the stripped time as the start time of the currently active log
                    startedTime = Long.parseLong(startedStr);
                    return true;
                } catch (Exception e) {
                    //Bad file name - just continue scanning
                }
            }
        }
        return false;
    }

    /**
     * Return the last active log file name, after we append to it the last documented start date, and the log
     * extension
     *
     * @return String - Last active log file name
     */
    @Override
    public String getActiveFileName() {
        return activeLogFilePath + "." + startedTime + LOG_EXT;
    }

    /**
     * Rollover current log file name, after we append the rollover time to it (so we can know the time each file
     * covers
     *
     * @throws RolloverFailure
     */
    @Override
    public void rollover() throws RolloverFailure {
        //Get last active file name (to rename it)
        String prevActiveFileName = getActiveFileName();

        //Keep last start date, since we update it now
        long previousStarted = startedTime;
        startedTime = System.currentTimeMillis();

        //Rename append start and end time to the last active file name
        util.rename(prevActiveFileName, activeLogFilePath + "." + previousStarted + TS_SEP + startedTime + LOG_EXT);
    }
}
