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

package org.artifactory.rest.resource.search.types;

import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.resource.ci.BuildResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Noam Y. Tenne
 */
public class DependencySearchResource {

    private RestAddon restAddon;
    private HttpServletRequest request;
    private AuthorizationService authService;

    public DependencySearchResource(RestAddon restAddon, HttpServletRequest request, AuthorizationService authService) {
        this.restAddon = restAddon;
        this.request = request;
        this.authService = authService;
    }

    @GET
    @Produces({SearchRestConstants.MT_DEPENDENCY_BUILDS, MediaType.APPLICATION_JSON})
    public Response get(@QueryParam(SearchRestConstants.PARAM_SHA1_CHECKSUM) String sha1) throws IOException {
        if (authService.isAnonUserAndAnonBuildInfoAccessDisabled()){
            throw new AuthorizationRestException(BuildResource.anonAccessDisabledMsg);
        }
        try {
            return restAddon.searchDependencyBuilds(request, sha1);
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        }
    }
}
