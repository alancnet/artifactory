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
import org.artifactory.api.search.gavc.GavcSearchControls;
import org.artifactory.api.search.gavc.GavcSearchResult;
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
 * Resource class that handles GAVC search actions
 *
 * @author Eli givoni
 */
public class GavcSearchResource {

    private AuthorizationService authorizationService;
    private SearchService searchService;
    private RepositoryService repositoryService;
    private RepositoryBrowsingService repoBrowsingService;
    private HttpServletRequest request;

    /**
     * @param searchService       Search service instance
     * @param repositoryService
     * @param repoBrowsingService
     */
    public GavcSearchResource(AuthorizationService authorizationService, SearchService searchService,
            RepositoryService repositoryService, RepositoryBrowsingService repoBrowsingService,
            HttpServletRequest request) {
        this.authorizationService = authorizationService;
        this.searchService = searchService;
        this.repositoryService = repositoryService;
        this.repoBrowsingService = repoBrowsingService;
        this.request = request;
    }

    /**
     * Parametrized GAVC search
     *
     * @param groupId       Group ID to search for
     * @param artifactId    Artifact ID to search for
     * @param version       Version to search for
     * @param classifier    Classifier to search for
     * @param reposToSearch Specific repositories to search in
     * @return Rest search results object
     */
    @GET
    @Produces({SearchRestConstants.MT_GAVC_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(
            @QueryParam(SearchRestConstants.PARAM_GAVC_GROUP_ID) String groupId,
            @QueryParam(SearchRestConstants.PARAM_GAVC_ARTIFACT_ID) String artifactId,
            @QueryParam(SearchRestConstants.PARAM_GAVC_VERSION) String version,
            @QueryParam(SearchRestConstants.PARAM_GAVC_CLASSIFIER) String classifier,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch)
            throws IOException {
        return search(groupId, artifactId, version, classifier, reposToSearch);
    }

    /**
     * Performs the GAVC search
     *
     * @param groupId       Group ID to search for
     * @param artifactId    Artifact ID to search for
     * @param version       Version to search for
     * @param classifier    Classifier to search for
     * @param reposToSearch Specific repositories to search in
     * @return Rest search results object
     */
    private Response search(String groupId, String artifactId, String version, String classifier,
            List<String> reposToSearch) throws IOException {
        if (hasAtLeastOneValidParameter(groupId, artifactId, version, classifier)) {
            GavcSearchControls searchControls = new GavcSearchControls();
            searchControls.setGroupId(groupId);
            searchControls.setArtifactId(artifactId);
            searchControls.setVersion(version);
            searchControls.setClassifier(classifier);
            searchControls.setLimitSearchResults(authorizationService.isAnonymous());
            searchControls.setSelectedRepoForSearch(reposToSearch);

            if (searchControls.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("The search term cannot be empty").build();
            }
            if (searchControls.isWildcardsOnly()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        "Search term containing only wildcards is not permitted").build();
            }

            ItemSearchResults<GavcSearchResult> searchResults;
            try {
                searchResults = searchService.searchGavc(searchControls);
            } catch (RepositoryRuntimeException e) {
                throw new NotFoundException(e.getMessage());
            }

            InfoRestSearchResult gavcRestSearchResult = new InfoRestSearchResult();
            for (GavcSearchResult result : searchResults.getResults()) {
                ItemInfo itemInfo = result.getItemInfo();
                StorageInfoHelper storageInfoHelper = new StorageInfoHelper(request, repositoryService,
                        repoBrowsingService,
                        itemInfo);
                gavcRestSearchResult.results.add(storageInfoHelper.createStorageInfo());
            }
            return Response.ok(gavcRestSearchResult).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(
                "Missing groupId or artifactId or version or classifier").build();
    }

    private boolean hasAtLeastOneValidParameter(String groupId, String artifactId, String version, String classifier) {
        return StringUtils.isNotBlank(groupId) || StringUtils.isNotBlank(artifactId) ||
                StringUtils.isNotBlank(version) || StringUtils.isNotBlank(classifier);
    }
}