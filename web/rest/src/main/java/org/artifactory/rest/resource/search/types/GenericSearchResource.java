/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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
import org.artifactory.api.rest.constant.ArtifactRestConstants;
import org.artifactory.api.rest.constant.RestConstants;
import org.artifactory.api.rest.search.common.RestDateFieldName;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Calendar;

import static org.artifactory.api.rest.constant.SearchRestConstants.NOT_FOUND;

/**
 * Date: 5/11/14 3:11 PM
 *
 * @author freds
 */
public abstract class GenericSearchResource {
    private static final Logger log = LoggerFactory.getLogger(GenericSearchResource.class);

    protected SearchService searchService;
    private AuthorizationService authorizationService;
    private HttpServletRequest request;

    public GenericSearchResource(SearchService searchService, AuthorizationService authorizationService,
            HttpServletRequest request) {
        this.searchService = searchService;
        this.authorizationService = authorizationService;
        this.request = request;
    }

    protected Response search(Long from, Long to, StringList reposToSearch, RestDateFieldName... dates) throws IOException {
        assertValidRequest(from);

        Calendar fromCal = null;
        if (from != null) {
            fromCal = Calendar.getInstance();
            fromCal.setTimeInMillis(from);
        }
        Calendar toCal = null;
        if (to != null) {
            toCal = Calendar.getInstance();
            toCal.setTimeInMillis(to);
        }
        ItemSearchResults<ArtifactSearchResult> results;
        try {
            results = searchService.searchArtifactsInRange(fromCal, toCal, reposToSearch, dates);
        } catch (RepositoryRuntimeException e) {
            String msg = "Got Exception retrieving artifacts date in range from=" + from + " to=" + to;
            log.error(msg, e);
            throw new RestException(msg + " due to " + e.getMessage());
        }

        if (results.getResults().isEmpty()) {
            throw new NotFoundException(NOT_FOUND);
        }
        return createResponse(results, fromCal, toCal, dates);
    }

    private void assertValidRequest(Long from) {
        if (authorizationService.isAnonymous()) {
            throw new AuthorizationRestException();
        }

        if (from == null) {
            throw new BadRequestException("'from' parameter cannot be empty!");
        }
    }

    protected abstract Response createResponse(ItemSearchResults<ArtifactSearchResult> results, Calendar from,
            Calendar to, RestDateFieldName[] dates);

    protected String buildStorageUri(RepoPath repoPath) {
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        StringBuilder sb = new StringBuilder(servletContextUrl);
        sb.append("/").append(RestConstants.PATH_API).append("/").append(ArtifactRestConstants.PATH_ROOT);
        sb.append("/").append(repoPath.getRepoKey()).append("/").append(repoPath.getPath());
        return sb.toString();
    }
}
