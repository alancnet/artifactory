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

package org.artifactory.api.repo.exception;

import org.artifactory.exception.CancelException;

/**
 * An exception thrown when a repo rejects the deployment or retrieval of an item.
 *
 * @author Noam Y. Tenne
 */
public class RepoRejectException extends Exception {

    private int status = 404;

    protected RepoRejectException() {
        super();
    }

    protected RepoRejectException(int status) {
        this.status = status;
    }

    /**
     * Error message constructor
     *
     * @param message Error message
     */
    public RepoRejectException(String message) {
        super(message);
    }

    /**
     * Error message constructor
     *
     * @param message Error message
     * @param status  The (http) error status
     */
    public RepoRejectException(String message, int status) {
        super(message);
        if (status > 0) {
            this.status = status;
        }
    }

    /**
     * Cause constructor
     *
     * @param cause The nested exception
     */
    public RepoRejectException(Throwable cause) {
        super(cause);
        if (cause instanceof CancelException) {
            status = ((CancelException) cause).getErrorCode();
        }
    }

    /**
     * Returns the HTTP error code associated with the thrown exception
     *
     * @return HTTP error code
     */
    public int getErrorCode() {
        return status;
    }
}