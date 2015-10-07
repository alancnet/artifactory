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

package org.artifactory.rest;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A JSON object to be sent as an error thrown by the REST API.
 *
 * @author Shay Yaakov
 */
public class ErrorResponse {

    List<Error> errors = Lists.newArrayList();

    public ErrorResponse(int status, String message) {
        errors.add(new Error(status, message != null ? message : ""));
    }

    public List<Error> getErrors() {
        return errors;
    }

    private static class Error {
        private int status = 500;
        private String message = "";

        private Error(int status) {
            this.status = status;
        }

        private Error(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
