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

package org.artifactory.exception;

/**
 * An exception used to indicate cancellation of an action with an appropriate status error code to return.
 * <p/>
 * Primarily used to reject deployment or retrieval of an item.
 *
 * @author Yoav Landman
 */
public class CancelException extends RuntimeException {

    private final int status;

    public CancelException(String message, int status) {
        this(message, null, status);
    }

    public CancelException(String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }


    /**
     * Returns the HTTP error code associated with the cancellation.
     *
     * @return HTTP error code
     */
    public int getErrorCode() {
        return status;
    }
}