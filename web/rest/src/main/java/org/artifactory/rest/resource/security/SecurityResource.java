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

package org.artifactory.rest.resource.security;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.rest.constant.SecurityRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.exception.RestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Noam Y. Tenne
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(SecurityRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class SecurityResource {

    private static final String ENTITY_TYPE = "entityType";
    private static final String ENTITY_KEY = "entityKey";
    private static final String INNER_PATH = "{" + ENTITY_TYPE + ": .+}/{" + ENTITY_KEY + ": .+}";

    @Autowired
    AddonsManager addonsManager;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    SecurityService securityService;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("{" + ENTITY_TYPE + ": .+}")
    @Produces({SecurityRestConstants.MT_USERS, SecurityRestConstants.MT_GROUPS,
            SecurityRestConstants.MT_PERMISSION_TARGETS, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getSecurityEntities(@PathParam(ENTITY_TYPE) String entityType) throws UnsupportedEncodingException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.getSecurityEntities(request, decodeEntityKey(entityType));
    }

    @GET
    @Path(INNER_PATH)
    @Produces({SecurityRestConstants.MT_USER, SecurityRestConstants.MT_GROUP,
            SecurityRestConstants.MT_PERMISSION_TARGET, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response getSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws UnsupportedEncodingException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.getSecurityEntity(entityType, decodeEntityKey(entityKey));
    }

    @DELETE
    @Path(INNER_PATH)
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response deleteSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws UnsupportedEncodingException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.deleteSecurityEntity(entityType, decodeEntityKey(entityKey));
    }

    @PUT
    @Path(INNER_PATH)
    @Consumes({SecurityRestConstants.MT_USER, SecurityRestConstants.MT_GROUP,
            SecurityRestConstants.MT_PERMISSION_TARGET, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response createOrReplaceSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws IOException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.createOrReplaceSecurityEntity(entityType, decodeEntityKey(entityKey), request);
    }

    @POST
    @Path(INNER_PATH)
    @Consumes({SecurityRestConstants.MT_USER, SecurityRestConstants.MT_GROUP,
            SecurityRestConstants.MT_PERMISSION_TARGET, MediaType.APPLICATION_JSON})
    @RolesAllowed({AuthorizationService.ROLE_ADMIN})
    public Response updateSecurityEntity(@PathParam(ENTITY_TYPE) String entityType,
            @PathParam(ENTITY_KEY) String entityKey) throws IOException {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.updateSecurityEntity(entityType, decodeEntityKey(entityKey), request);
    }

    @GET
    @Path("encryptedPassword")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getEncryptedPassword() {
        if (!securityService.isPasswordEncryptionEnabled()) {
            throw new RestException(HttpStatus.SC_CONFLICT, "Server doesn't support encrypted passwords");
        }

        String encryptedPassword = authorizationService.currentUserEncryptedPassword(false);
        if (StringUtils.isNotBlank(encryptedPassword)) {
            return Response.ok(encryptedPassword).build();
        }

        throw new NotFoundException("User not found: " + authorizationService.currentUsername());
    }

    private String decodeEntityKey(String entityKey) throws UnsupportedEncodingException {
        return URLDecoder.decode(entityKey, CharEncoding.UTF_8);
    }
}
