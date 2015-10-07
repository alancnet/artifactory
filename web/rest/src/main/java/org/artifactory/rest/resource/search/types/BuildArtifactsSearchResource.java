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

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.constant.BuildRestConstants;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.result.DownloadRestSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.resource.ci.BuildResource;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Resource for retrieving build artifacts according to input regexp patterns.
 *
 * @author Shay Yaakov
 * @see BuildArtifactsRequest
 */
public class BuildArtifactsSearchResource {

    private final RestAddon restAddon;
    private AuthorizationService authorizationService;
    private HttpServletRequest request;

    public BuildArtifactsSearchResource(RestAddon restAddon, AuthorizationService authorizationService,
            HttpServletRequest request) {
        this.restAddon = restAddon;
        this.authorizationService = authorizationService;
        this.request = request;
    }

    @POST
    @Consumes({BuildRestConstants.MT_BUILD_ARTIFACTS_REQUEST, MediaType.APPLICATION_JSON})
    @Produces({SearchRestConstants.MT_BUILD_ARTIFACTS_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(BuildArtifactsRequest buildArtifactsRequest) throws IOException {

        if (authorizationService.isAnonUserAndAnonBuildInfoAccessDisabled()) {
            throw new AuthorizationRestException(BuildResource.anonAccessDisabledMsg);
        }
        if (isBlank(buildArtifactsRequest.getBuildName())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cannot search without build name.").build();
        }
        boolean buildNumberIsBlank = isBlank(buildArtifactsRequest.getBuildNumber());
        boolean buildStatusIsBlank = isBlank(buildArtifactsRequest.getBuildStatus());
        if (buildNumberIsBlank && buildStatusIsBlank) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    "Cannot search without build number or build status.").build();
        }
        if (!buildNumberIsBlank && !buildStatusIsBlank) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    "Cannot search with both build number and build status parameters, " +
                            "please omit build number if your are looking for latest build by status " +
                            "or omit build status to search for specific build version.").build();
        }

        if (!authorizationService.isAuthenticated()) {
            throw new AuthorizationRestException();
        }

        Map<FileInfo, String> buildArtifacts;
        try {
            buildArtifacts = restAddon.getBuildArtifacts(buildArtifactsRequest);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getLocalizedMessage()).build();
        }
        if (buildArtifacts == null || buildArtifacts.isEmpty()) {
            throw new NotFoundException(String.format("Could not find any build artifacts for build '%s' number '%s'.",
                                buildArtifactsRequest.getBuildName(),
                                buildArtifactsRequest.getBuildNumber()));
        }

        DownloadRestSearchResult downloadRestSearchResult = new DownloadRestSearchResult();
        for (FileInfo fileInfo : buildArtifacts.keySet()) {
            String downloadUri = RestUtils.buildDownloadUri(request, fileInfo.getRepoKey(), fileInfo.getRelPath());
            downloadRestSearchResult.results.add(new DownloadRestSearchResult.SearchEntry(downloadUri));
        }

        return Response.ok(downloadRestSearchResult).build();
    }
}
