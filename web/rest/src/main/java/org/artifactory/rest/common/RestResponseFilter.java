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

package org.artifactory.rest.common;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.rest.util.AuthUtils;
import org.artifactory.storage.StorageService;
import org.artifactory.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 * Intercepts all Jersey client error responses (status code >= 400) and sends the response
 * as a JSON object. Also adds a response entity in case it was not specified.
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RestResponseFilter implements ContainerResponseFilter {

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AuthorizationService authenticationService;

    private GlobalMessageProvider messageProvider;

    public RestResponseFilter() {
        messageProvider=new GlobalMessageProvider();
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        int status = response.getStatus();
        // add message to response headers
        String baseUrl = PathUtils.trimTrailingSlashes(request.getBaseUri().getPath());
        if(baseUrl.endsWith("ui")) {
            messageProvider.decorateWithGlobalMessages(response, addonsManager, storageService, authenticationService);
        }
        AuthUtils.addSessionStatusToHeaders(response, uriInfo, this.request);
        if (status >= 400) {
            Object entity = response.getEntity();
            if (entity == null) {
                ErrorResponse errorResponse = new ErrorResponse(status, response.getStatusType().getReasonPhrase());
                response.setResponse(createJsonErrorResponse(response.getResponse(), errorResponse));
            } else if (entity instanceof String && !MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
                ErrorResponse errorResponse = new ErrorResponse(status, (String) entity);
                response.setResponse(createJsonErrorResponse(response.getResponse(), errorResponse));
            }
        }

        return response;
    }
    private Response createJsonErrorResponse(Response response, ErrorResponse errorResponse) {
        return Response.fromResponse(response).entity(errorResponse).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
