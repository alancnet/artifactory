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

package org.artifactory.schedule;

/**
 * Thrown by a running job when it is interrupted and it needs to throw an exception to break the operation.
 *
 * @author Yossi Shaul
 */
public class TaskInterruptedException extends RuntimeException {

    public TaskInterruptedException() {
        super("Task " + TaskCallback.currentTaskToken() + " interrupted.");
    }

    public TaskInterruptedException(String message) {
        super(message);
    }

    public TaskInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
