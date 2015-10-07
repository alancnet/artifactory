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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.result.ArchiveRestSearchResult;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

import com.google.common.collect.Lists;

import static org.artifactory.api.rest.constant.SearchRestConstants.NOT_FOUND;

/**
 * Resource class that handles archive search actions
 *
 * @author Eli givoni
 */
public class ArchiveSearchResource {

    private AuthorizationService authorizationService;
    private SearchService searchService;
    private HttpServletRequest request;

    /**
     * Main constructor
     *
     * @param searchService Search service instance
     */
    public ArchiveSearchResource(AuthorizationService authorizationService, SearchService searchService,
            HttpServletRequest request) {
        this.authorizationService = authorizationService;
        this.searchService = searchService;
        this.request = request;
    }

    /**
     * Parametrized archive search
     *
     * @param name          Entry name to search for
     * @param reposToSearch Specific repositories to search in
     * @return Rest search results object
     */
    @GET
    @Produces({SearchRestConstants.MT_ARCHIVE_ENTRY_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(
            @QueryParam(SearchRestConstants.PARAM_SEARCH_NAME) String name,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch) throws IOException {
        return search(name, reposToSearch);
    }


    /**
     * Performs the archive search
     *
     * @param name          Entry name to search for
     * @param reposToSearch Specific repositories to search in  @return Rest search results object
     */
    private Response search(String name, List<String> reposToSearch) throws IOException {
        ArchiveSearchControls controls = new ArchiveSearchControls();
        controls.setName(name);
        controls.setLimitSearchResults(authorizationService.isAnonymous());
        controls.setSelectedRepoForSearch(reposToSearch);

        if (controls.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("The search term cannot be empty").build();
        }
        if (controls.isWildcardsOnly()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Search term containing only wildcards is not permitted").build();
        }

        ItemSearchResults<ArchiveSearchResult> searchResults;
        try {
            searchResults = searchService.searchArchiveContent(controls);
        } catch (RepositoryRuntimeException e) {
            throw new NotFoundException(e.getMessage());
        }
        //TODO: [by ys] use multi map
        List<ArchiveSearchResult> results = searchResults.getResults();
        if (results.isEmpty()) {
            throw new NotFoundException(NOT_FOUND);
        }

        MutableSortDefinition definition = new MutableSortDefinition("entry", false, true);
        PropertyComparator.sort(results, definition);
        ArchiveRestSearchResult archiveRestSearchResult = new ArchiveRestSearchResult();
        String entryToCheck = results.get(0).getEntryPath();
        List<String> fileInfoUris = Lists.newArrayList();
        for (ArchiveSearchResult result : results) {
            //aggregate all uri for the same entry
            if (entryToCheck.equals(result.getEntryPath())) {
                fileInfoUris.add(RestUtils.buildStorageInfoUri(request, result));
            } else {
                //add ArchiveEntry to ArchiveRestSearchResult
                ArchiveRestSearchResult.ArchiveEntry archiveEntry =
                        new ArchiveRestSearchResult.ArchiveEntry(entryToCheck, fileInfoUris);
                archiveRestSearchResult.results.add(archiveEntry);
                entryToCheck = result.getEntryPath();
                fileInfoUris = Lists.newArrayList(RestUtils.buildStorageInfoUri(request, result));
            }
        }
        //add ArchiveEntry for the last iteration
        ArchiveRestSearchResult.ArchiveEntry archiveEntry =
                new ArchiveRestSearchResult.ArchiveEntry(entryToCheck, fileInfoUris);
        archiveRestSearchResult.results.add(archiveEntry);

        return Response.ok(archiveRestSearchResult).build();
    }
}