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

package org.artifactory.addon.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Authorization exception (403 Forbidden) for REST API usage
 * <p/>
 * <strong>NOTE!</strong> Do not attempt to set an entity since {@link org.artifactory.rest.common.exception.ArtifactoryRestExceptionMapper}
 * will not intercept it for manipulating the response status.
 * Jersey exception mappers only intercept exceptions without an entity in them.
 *
 * @author Shay Yaakov
 */
public class AuthorizationRestException extends WebApplicationException {

    public AuthorizationRestException() {
        super(Response.status(Response.Status.FORBIDDEN).build());
    }

    public AuthorizationRestException(String message) {
        super(Response.status(Response.Status.FORBIDDEN).entity(message).build());
    }
}
