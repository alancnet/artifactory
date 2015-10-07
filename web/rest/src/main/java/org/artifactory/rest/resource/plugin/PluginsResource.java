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

package org.artifactory.rest.resource.plugin;

import com.google.common.collect.Maps;
import com.sun.jersey.api.Responses;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.ResponseCtx;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.io.SimpleResourceStreamHandle;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.ErrorResponse;
import org.artifactory.rest.common.list.KeyValueList;
import org.artifactory.util.StringInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static org.artifactory.api.rest.constant.PluginRestConstants.*;

/**
 * A resource for plugin execution
 *
 * @author Tomer Cohen
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path(PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
public class PluginsResource {

    @Autowired
    AddonsManager addonsManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginInfo() {
        return getPluginInfo(null);
    }

    @GET
    @Path("{pluginType: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginInfo(@Nullable @PathParam("pluginType") String pluginType) {
        return addonsManager.addonByType(RestAddon.class).getUserPluginInfo(pluginType);
    }

    @POST
    @Consumes(WILDCARD)
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response execute(@Context Request request,
            InputStream body,
            @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        try (ResourceStreamHandle handle = new SimpleResourceStreamHandle(body)) {
            ResponseCtx responseCtx =
                    addonsManager.addonByType(RestAddon.class).runPluginExecution(executionName, request.getMethod(),
                            params,
                            handle, async == 1);
            if (async == 1) {
                //Just return accepted (202)
                return Response.status(HttpStatus.SC_ACCEPTED).build();
            } else {
                return responseFromResponseCtx(responseCtx);
            }
        }
    }

    @PUT
    @Consumes(WILDCARD)
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response executePut(@Context Request request,
            InputStream body,
            @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        return execute(request, body, executionName, paramsList, async);
    }

    @GET
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response execute(@Context Request request, @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).runPluginExecution(executionName,
                request.getMethod(), params, null, async == 1);
        if (async == 1) {
            //Just return accepted (202)
            return Response.status(HttpStatus.SC_ACCEPTED).build();
        } else {
            return responseFromResponseCtx(responseCtx);
        }
    }

    @DELETE
    @Path(PATH_EXECUTE + "/{executionName: .+}")
    @Produces(TEXT_PLAIN)
    public Response executeDelete(@Context Request request, @PathParam("executionName") String executionName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList,
            @QueryParam(PARAM_ASYNC) int async) throws Exception {
        return execute(request, executionName, paramsList, async);
    }

    @GET
    @Path(PATH_STAGING + "/{strategyName: .+}")
    @Produces({MT_BUILD_STAGING_STRATEGY, MediaType.APPLICATION_JSON})
    public Response getBuildStagingStrategy(
            @PathParam("strategyName") String strategyName,
            @QueryParam("buildName") String buildName,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList) {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        return addonsManager.addonByType(RestAddon.class).getStagingStrategy(strategyName, buildName, params);
    }

    @POST
    @Path(PATH_PROMOTE + "/{promotionName: .+}/{buildName: .+}/{buildNumber: .+}")
    @Produces(TEXT_PLAIN)
    public Response promote(
            @PathParam("promotionName") String promotionName,
            @PathParam("buildName") String buildName,
            @PathParam("buildNumber") String buildNumber,
            @QueryParam(PARAM_PARAMS) KeyValueList paramsList) {
        Map<String, List<String>> params =
                paramsList != null ? paramsList.toStringMap() : Maps.<String, List<String>>newHashMap();
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).promote(promotionName, buildName,
                buildNumber, params);
        return responseFromResponseCtx(responseCtx);
    }

    @PUT
    @Path("{scriptName: .+}")
    @Consumes({"text/x-groovy", TEXT_PLAIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deploy(Reader pluginScript, @PathParam("scriptName") String scriptName) {
        ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).deployPlugin(pluginScript, scriptName);
        if (responseCtx.getStatus() >= 400) {
            ErrorResponse errorResponse = new ErrorResponse(responseCtx.getStatus(), responseCtx.getMessage());
            return Responses.clientError().type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
        } else {
            return responseFromResponseCtx(responseCtx);
        }
    }

    @POST
    @Path("reload")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed(AuthorizationService.ROLE_ADMIN)
    public Response reload() {
        try {
            ResponseCtx responseCtx = addonsManager.addonByType(RestAddon.class).reloadPlugins();
            return Response.status(responseCtx.getStatus())
                    // ugly hack to force text response (to overcome our default in org.artifactory.rest.common.RestErrorResponseFilter
                    .entity(new StringInputStream(responseCtx.getMessage()))
                    .type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    private Response responseFromResponseCtx(ResponseCtx responseCtx) {
        Response.ResponseBuilder builder;
        int status = responseCtx.getStatus();
        if (status != ResponseCtx.UNSET_STATUS) {
            builder = Response.status(status);
        } else {
            builder = Response.ok();
        }
        String message = responseCtx.getMessage();
        if (message != null) {
            builder.entity(message);
        }
        return builder.build();
    }
}
