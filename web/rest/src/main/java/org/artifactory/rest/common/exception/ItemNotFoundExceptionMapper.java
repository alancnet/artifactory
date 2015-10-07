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

import com.sun.jersey.api.Responses;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.rest.ErrorResponse;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Intercepts and maps {@link ItemNotFoundRuntimeException} exceptions thrown by the UI.
 *
 * @author Dan Feldman
 */
@Component
@Provider
public class ItemNotFoundExceptionMapper implements ExceptionMapper<ItemNotFoundRuntimeException> {

    @Override
    public Response toResponse(ItemNotFoundRuntimeException exception) {
        ErrorResponse errorResponse = new ErrorResponse(Response.Status.NOT_FOUND.getStatusCode(),
                exception.getMessage());
        return Responses.notFound().type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
    }
}
