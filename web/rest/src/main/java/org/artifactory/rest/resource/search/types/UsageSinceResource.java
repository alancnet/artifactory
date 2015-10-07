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
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.result.LastDownloadRestResult;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.stats.StatsSearchControls;
import org.artifactory.api.search.stats.StatsSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

import static org.artifactory.api.rest.constant.SearchRestConstants.NOT_FOUND;

/**
 * @author Eli Givoni
 */
public class UsageSinceResource {
    private AuthorizationService authorizationService;
    private SearchService searchService;
    private HttpServletRequest request;

    public UsageSinceResource(AuthorizationService authorizationService, SearchService searchService,
            HttpServletRequest request) {
        this.authorizationService = authorizationService;
        this.searchService = searchService;
        this.request = request;
    }

    @GET
    @Produces({SearchRestConstants.MT_USAGE_SINCE_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(
            @QueryParam(SearchRestConstants.PARAM_SEARCH_NOT_USED_SINCE) Long lastDownloaded,
            @QueryParam(SearchRestConstants.PARAM_CREATED_BEFORE) Long createdBefore,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch) throws IOException {
        if (authorizationService.isAnonymous()) {
            throw new AuthorizationRestException();
        }
        return search(lastDownloaded, createdBefore, reposToSearch);
    }

    private Response search(Long lastDownloaded, Long createdBefore, List<String> reposToSearch)
            throws IOException {
        if (lastDownloaded == null) {
            throw new NotFoundException(NOT_FOUND);
        }

        StatsSearchControls searchControls = new StatsSearchControls();
        searchControls.setSelectedRepoForSearch(reposToSearch);
        searchControls.setLimitSearchResults(authorizationService.isAnonymous());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lastDownloaded);
        searchControls.setDownloadedSince(cal);

        if (createdBefore != null) {
            Calendar createdBeforeCalendar = Calendar.getInstance();
            createdBeforeCalendar.setTimeInMillis(createdBefore);
            searchControls.setCreatedBefore(createdBeforeCalendar);
        }
        List<StatsSearchResult> results;
        try {
            results = searchService.searchArtifactsNotDownloadedSince(searchControls).getResults();
        } catch (RepositoryRuntimeException e) {
            throw new NotFoundException(e.getMessage());
        }

        if (results.isEmpty()) {
            throw new NotFoundException(NOT_FOUND);
        }

        // sort by last downloaded
        MutableSortDefinition definition = new MutableSortDefinition("metadataObject.lastDownloaded", true, true);
        PropertyComparator.sort(results, definition);
        LastDownloadRestResult lastDownloadRestResult = new LastDownloadRestResult();
        for (StatsSearchResult result : results) {
            String uri = RestUtils.buildStorageInfoUri(request, result);
            String lDownloaded = RestUtils.toIsoDateString(result.getLastDownloaded());
            LastDownloadRestResult.DownloadedEntry entry = new LastDownloadRestResult.DownloadedEntry(uri, lDownloaded);
            lastDownloadRestResult.results.add(entry);
        }

        return Response.ok(lastDownloadRestResult).build();
    }
}
