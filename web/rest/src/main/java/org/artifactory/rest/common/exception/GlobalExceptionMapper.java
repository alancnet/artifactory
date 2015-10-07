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

package org.artifactory.rest.common.exception;

import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Intercepts and maps any un-caught exceptions thrown by the REST API.
 *
 * @author Shay Yaakov
 */
@Component
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        String message = exception.getMessage();
        log.error(message, exception);
        if (StringUtils.isBlank(message)) {
            message = "An unexpected error has occurred, please check Artifactory logs for further details.";
        }
        ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(
                errorResponse).build();
    }
}
