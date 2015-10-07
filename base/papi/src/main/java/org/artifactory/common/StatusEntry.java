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

package org.artifactory.common;

import java.io.Serializable;

/**
 * @author freds
 * @date Sep 25, 2008
 */
public class StatusEntry implements Serializable {
    private final int statusCode;
    private final StatusEntryLevel level;
    private final String message;
    private final Throwable exception;

    public StatusEntry(int statusCode, String message) {
        this(statusCode, StatusEntryLevel.INFO, message, null);
    }

    public StatusEntry(int statusCode, String message, Throwable exception) {
        this(statusCode, StatusEntryLevel.ERROR, message, exception);
    }

    public StatusEntry(int statusCode, StatusEntryLevel level, String message,
            Throwable exception) {
        if (level == null) {
            throw new IllegalArgumentException("Cannot create status entry '" + message + "' with null level");
        }
        this.statusCode = statusCode;
        this.level = level;
        this.message = message;
        this.exception = exception;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isWarning() {
        return level.isWarning();
    }

    public boolean isError() {
        return level.isError();
    }

    public boolean isDebug() {
        return level.isDebug();
    }

    public boolean isInfo() {
        return level.isInfo();
    }

    public StatusEntryLevel getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "StatusMessage{" +
                "statusCode=" + statusCode +
                ", level=" + level.name() +
                ", statusMsg='" + message + '\'' +
                ", exception=" + exception +
                '}';
    }
}
