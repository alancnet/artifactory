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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.result.InfoRestSearchResult;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.util.StorageInfoHelper;
import org.artifactory.sapi.common.RepositoryRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * Resource class that handles artifact search actions
 *
 * @author Eli givoni
 */
public class ArtifactSearchResource {

    private AuthorizationService authorizationService;
    private SearchService searchService;
    private RepositoryService repositoryService;
    private RepositoryBrowsingService repoBrowsingService;
    private HttpServletRequest request;

    /**
     * Main constructor
     *
     * @param searchService       Search service instance
     * @param repositoryService
     * @param repoBrowsingService
     */
    public ArtifactSearchResource(AuthorizationService authorizationService, SearchService searchService,
            RepositoryService repositoryService, RepositoryBrowsingService repoBrowsingService,
            HttpServletRequest request) {
        this.authorizationService = authorizationService;
        this.searchService = searchService;
        this.repositoryService = repositoryService;
        this.repoBrowsingService = repoBrowsingService;
        this.request = request;
    }

    /**
     * Parametrized artifact search
     *
     * @param name          to search for
     * @param reposToSearch Specific repositories to search in
     * @return Rest search results object
     */
    @GET
    @Produces({SearchRestConstants.MT_ARTIFACT_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(
            @QueryParam(SearchRestConstants.PARAM_SEARCH_NAME) String name,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch) throws IOException {
        return search(name, reposToSearch);
    }


    /**
     * Performs the artifact search
     *
     * @param name          Artifact ID to search for
     * @param reposToSearch Specific repositories to search in
     * @return Rest search results object
     */
    private Response search(String name, List<String> reposToSearch) throws IOException {
        ArtifactSearchControls controls = new ArtifactSearchControls();
        controls.setQuery(appendAndReturnWildcards(name));
        controls.setLimitSearchResults(authorizationService.isAnonymous());
        controls.setSelectedRepoForSearch(reposToSearch);

        if (controls.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("The search term cannot be empty").build();
        }
        if (controls.isWildcardsOnly()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    "Search term containing only wildcards is not permitted").build();
        }

        ItemSearchResults<ArtifactSearchResult> searchResults;
        try {
            searchResults = searchService.searchArtifacts(controls);
        } catch (RepositoryRuntimeException e) {
            throw new NotFoundException(e.getMessage());
        }
        InfoRestSearchResult result = new InfoRestSearchResult();
        for (ArtifactSearchResult searchResult : searchResults.getResults()) {
            ItemInfo itemInfo = searchResult.getItemInfo();
            StorageInfoHelper storageInfoHelper = new StorageInfoHelper(request, repositoryService, repoBrowsingService,
                    itemInfo);
            result.results.add(storageInfoHelper.createStorageInfo());
        }

        return Response.ok(result).build();
    }

    private String appendAndReturnWildcards(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        StringBuilder queryBuilder = new StringBuilder();
        if (!name.startsWith("*") && !name.startsWith("?")) {
            queryBuilder.append("*");
        }

        queryBuilder.append(name);

        if (!name.endsWith("*") && !name.endsWith("?")) {
            queryBuilder.append("*");
        }

        return queryBuilder.toString();
    }
}