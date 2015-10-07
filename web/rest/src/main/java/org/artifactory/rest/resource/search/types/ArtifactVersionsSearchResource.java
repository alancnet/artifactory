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

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.result.ArtifactVersionsResult;
import org.artifactory.api.rest.search.result.VersionEntry;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.list.StringList;
import org.springframework.util.AntPathMatcher;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * Resource class that handles artifact versions search action
 *
 * @author Shay Yaakov
 */
public class ArtifactVersionsSearchResource {
    private RestAddon restAddon;

    public ArtifactVersionsSearchResource(RestAddon restAddon) {
        this.restAddon = restAddon;
    }

    @GET
    @Produces({SearchRestConstants.MT_ARTIFACT_VERSIONS_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(
            @QueryParam(SearchRestConstants.PARAM_GAVC_GROUP_ID) String groupId,
            @QueryParam(SearchRestConstants.PARAM_GAVC_ARTIFACT_ID) String artifactId,
            @QueryParam(SearchRestConstants.PARAM_GAVC_VERSION) String version,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch,
            @QueryParam(SearchRestConstants.PARAM_FETCH_FROM_REMOTE) int remote)
            throws IOException {
        try {
            boolean useRemote = remote == 1;
            ArtifactVersionsResult artifactVersions = restAddon.getArtifactVersions(groupId, artifactId, version,
                    reposToSearch, useRemote, true);
            if (artifactVersions.getResults().isEmpty()) {
                throw new NotFoundException("Unable to find artifact versions");
            } else {
                char[] wildcardsArr = {'*', '?'};
                if (StringUtils.containsAny(version, wildcardsArr)) {
                    matchByPattern(artifactVersions, version);
                }
                return Response.ok(artifactVersions).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    private void matchByPattern(ArtifactVersionsResult artifactVersions, String version) {
        List<VersionEntry> filteredResults = Lists.newArrayList();
        AntPathMatcher matcher = new AntPathMatcher();
        for (VersionEntry result : artifactVersions.getResults()) {
            if (matcher.match(version, result.getVersion())) {
                filteredResults.add(result);
            }
        }

        artifactVersions.setResults(filteredResults);
    }
}
