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

package org.artifactory.api.common;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.common.StatusEntry;
import org.artifactory.common.StatusEntryLevel;
import org.artifactory.exception.CancelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Yoav Landman
 */
@XStreamAlias("status")
public class BasicStatusHolder implements MutableStatusHolder {
    private static final Logger log = LoggerFactory.getLogger(BasicStatusHolder.class);

    private static final String MSG_IDLE = "Idle.";
    // save up to 500 messages. if exhausted, we manually drop the oldest element
    private final ArrayBlockingQueue<StatusEntry> statusEntries = new ArrayBlockingQueue<>(500);
    // save up to 2000 errors messages. if exhausted, we manually drop the oldest element
    private final ArrayBlockingQueue<StatusEntry> errorEntries = new ArrayBlockingQueue<>(2000);
    // save up to 100 warning messages. if exhausted, we manually drop the oldest element
    private final ArrayBlockingQueue<StatusEntry> warningEntries = new ArrayBlockingQueue<>(100);

    public static final int CODE_OK = 200;
    public static final int CODE_INTERNAL_ERROR = 500;

    protected boolean activateLogging;
    // the latest status
    protected boolean fastFail = false;
    protected boolean verbose = false;
    private StatusEntry lastStatusEntry;
    private StatusEntry lastErrorStatusEntry;
    private StatusEntry lastWarningStatusEntry;

    public BasicStatusHolder() {
        //addStatusEntry(new StatusEntry(CODE_OK, StatusEntryLevel.DEBUG, MSG_IDLE, null));
        activateLogging = true;
    }

    @Override
    public void setFastFail(boolean fastFail) {
        this.fastFail = fastFail;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private boolean isFastFail() {
        return fastFail;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public StatusEntry getLastError() {
        return lastErrorStatusEntry;
    }

    @Override
    public StatusEntry getLastWarning() {
        return lastWarningStatusEntry;
    }

    @Override
    public StatusEntry getLastStatusEntry() {
       return lastStatusEntry;
    }

    @Override
    public final void debug(String statusMsg, @Nonnull Logger logger) {
        logEntryAndAddEntry(new StatusEntry(CODE_OK, StatusEntryLevel.DEBUG, statusMsg, null), logger);
    }

    public final void setDebug(String statusMsg, int statusCode, @Nonnull Logger logger) {
        logEntryAndAddEntry(new StatusEntry(statusCode, StatusEntryLevel.DEBUG, statusMsg, null), logger);
    }

    @Override
    public final void status(String statusMsg, @Nonnull Logger logger) {
        status(statusMsg, CODE_OK, logger);
    }

    @Override
    public final void status(String statusMsg, int statusCode, @Nonnull Logger logger) {
        logEntryAndAddEntry(new StatusEntry(statusCode, statusMsg), logger);
    }

    @Override
    public void error(String status, Throwable throwable, @Nonnull Logger logger) {
        error(status, CODE_INTERNAL_ERROR, throwable, logger);
    }

    @Override
    public void error(String statusMsg, @Nonnull Logger logger) {
        error(statusMsg, CODE_INTERNAL_ERROR, null, logger);
    }

    @Override
    public void error(String statusMsg, int statusCode, @Nonnull Logger logger) {
        error(statusMsg, statusCode, null, logger);
    }

    @Override
    public void error(String statusMsg, int statusCode, Throwable throwable, @Nonnull Logger logger) {
        addError(new StatusEntry(statusCode, StatusEntryLevel.ERROR, statusMsg, throwable), logger);
    }


    @Override
    public void warn(String statusMsg, Throwable throwable, @Nonnull Logger logger) {
        addError(new StatusEntry(CODE_INTERNAL_ERROR, StatusEntryLevel.WARNING, statusMsg, throwable), logger);
    }

    @Override
    public void warn(String statusMsg, @Nonnull Logger logger) {
        addError(new StatusEntry(CODE_INTERNAL_ERROR, StatusEntryLevel.WARNING, statusMsg, null), logger);
    }

    protected StatusEntry addError(@Nonnull StatusEntry errorEntry, @Nonnull Logger logger) {
        if (isActivateLogging()) {
            logEntry(errorEntry, logger);
        }
        addStatusEntry(errorEntry);
        if (!errorEntry.isWarning() && isFastFail()) {
            Throwable throwable = errorEntry.getException();
            if (throwable != null) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                } else if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else {
                    throw new RuntimeException("Fast fail exception: " + errorEntry.getMessage(), throwable);
                }
            } else {
                throw new RuntimeException("Fast fail exception: " + errorEntry.getMessage());
            }
        }
        return errorEntry;
    }

    protected void logEntry(@Nonnull StatusEntry entry, @Nonnull Logger logger) {
        /**
         * If an external logger is given, it shall be the active one; unless verbose output is requested, then we need
         * to use that status holder logger for the debug level
         */
        if (isVerbose()) {
            doLogEntry(entry, log);
        } else {
            doLogEntry(entry, logger);
        }
    }

    protected void doLogEntry(@Nonnull StatusEntry entry, @Nonnull Logger logger) {
        String statusMessage = entry.getMessage();
        Throwable throwable = entry.getException();
        if (!isVerbose() && throwable != null) {
            //Update the status message for when there's an exception message to append
            statusMessage += ": " + (StringUtils.isNotBlank(throwable.getMessage()) ? throwable.getMessage() :
                    throwable.getClass().getSimpleName());
        }
        if (entry.isWarning() && logger.isWarnEnabled()) {
            if (isVerbose()) {
                logger.warn(statusMessage, throwable);
            } else {
                logger.warn(statusMessage);
            }
        } else if (entry.isError() && logger.isErrorEnabled()) {
            if (isVerbose()) {
                logger.error(statusMessage, throwable);
            } else {
                logger.error(statusMessage);
            }
        } else if (entry.isDebug() && logger.isDebugEnabled()) {
            logger.debug(statusMessage);
        } else if (entry.isInfo() && logger.isInfoEnabled()) {
            logger.info(statusMessage);
        }
    }

    @Override
    public String getStatusMsg() {
        StatusEntry lastError = getLastError();
        if (lastError != null) {
            return lastError.getMessage();
        }
        StatusEntry lastWarning = getLastWarning();
        if (lastWarning != null) {
            return lastWarning.getMessage();
        }
        StatusEntry statusEntry = getLastStatusEntry();
        return statusEntry!=null?statusEntry.getMessage():null;
    }

    @Override
    public boolean isError() {
        return getLastError()!=null;
    }

    @Override
    public CancelException getCancelException() {
        return getCancelException(null);
    }

    public CancelException getCancelException(StatusEntry previousToLastError) {
        StatusEntry lastError = getLastError();
        if (lastError != null && !lastError.equals(previousToLastError)) {
            //We have a new error check if it is a cancellation one
            Throwable cause = lastError.getException();
            if (cause != null && cause instanceof CancelException) {
                return (CancelException) cause;
            }
        }
        return null;
    }

    @Override
    public Throwable getException() {
        StatusEntry lastError = getLastError();
        if (lastError != null) {
            return lastError.getException();
        }
        StatusEntry lastWarning = getLastWarning();
        if (lastWarning != null) {
            return lastWarning.getException();
        }
        StatusEntry statusEntry = getLastStatusEntry();
        return statusEntry!=null?statusEntry.getException():null;
    }

    @Override
    public int getStatusCode() {
        StatusEntry lastError = getLastError();
        if (lastError != null) {
            return lastError.getStatusCode();
        }
        StatusEntry lastWarning = getLastWarning();
        if (lastWarning != null) {
            return lastWarning.getStatusCode();
        }
        StatusEntry statusEntry = getLastStatusEntry();
        return statusEntry!=null?statusEntry.getStatusCode():-1;
    }

    protected void logEntryAndAddEntry(@Nonnull StatusEntry entry, @Nonnull Logger logger) {
        addStatusEntry(entry);
        logEntry(entry, logger);
    }

    protected void addStatusEntry(StatusEntry entry) {
        // we don't really want to block if we reached the limit. remove the last element until offer is accepted
        while (!statusEntries.offer(entry)) {
            statusEntries.poll();
        }
        lastStatusEntry=entry;
        if (entry.isError()) {
            while (!errorEntries.offer(entry)) {
                errorEntries.poll();
            }
            lastErrorStatusEntry=entry;
        }
        else if (entry.isWarning()) {
            while (!warningEntries.offer(entry)) {
                warningEntries.poll();
            }
            lastWarningStatusEntry=entry;
        }
    }

    /**
     * @return True if the status holder prints the messages to the logger.
     */
    private boolean isActivateLogging() {
        return activateLogging;
    }

    /**
     * If set to false the status holder will not print the messages to the logger. It will only keep the statuses.
     *
     * @param activateLogging Set to false to disable logging
     */
    @Override
    public void setActivateLogging(boolean activateLogging) {
        this.activateLogging = activateLogging;
    }

    @Override
    public void reset() {
        statusEntries.clear();
        errorEntries.clear();
        warningEntries.clear();
        activateLogging = true;
    }

    @Override
    public String toString() {
        return "StatusHolder{" +
                "activateLogging=" + activateLogging +
                ", statusMessage=" + statusEntries + '}'+
                ", errorMessage=" + errorEntries + '}'+
                ", warningMessage=" + warningEntries + '}';
    }

    /**
     * Merge this and the input status. Will append entry from the input this one. If the status to merge has last error
     * it will be used. This method is not thread safe, the two statuses are assumed to be inactive in the time of
     * merging.
     *
     * @param toMerge The status to merge into this.
     */
    public void merge(BasicStatusHolder toMerge) {
        for (StatusEntry statusEntry : toMerge.statusEntries) {
            while (!statusEntries.offer(statusEntry)) {
                statusEntries.poll();
            }
        }
        lastStatusEntry=toMerge.statusEntries.peek();
        for (StatusEntry statusEntry : toMerge.errorEntries) {
            while (!errorEntries.offer(statusEntry)) {
                errorEntries.poll();
            }
        }
        lastErrorStatusEntry=toMerge.errorEntries.peek();
        for (StatusEntry statusEntry : toMerge.warningEntries) {
            while (!warningEntries.offer(statusEntry)) {
                warningEntries.poll();
            }

        }
        lastWarningStatusEntry=toMerge.warningEntries.peek();
    }

    public List<StatusEntry> getEntries() {
        return Lists.newArrayList(statusEntries);
    }

    public List<StatusEntry> getEntries(StatusEntryLevel level) {
        List<StatusEntry> result = new ArrayList<>();
        if (level == StatusEntryLevel.ERROR) {
            result.addAll(errorEntries);
        } else {
            for (StatusEntry entry : statusEntries) {
                if (level.equals(entry.getLevel())) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public boolean hasWarnings() {
        return warningEntries.size()>0;
    }

    public boolean hasErrors() {
        return errorEntries.size()>0;
    }

    public List<StatusEntry> getErrors() {
        return Lists.newArrayList(errorEntries);
    }

    public List<StatusEntry> getWarnings() {
        return Lists.newArrayList(warningEntries);
    }
}
