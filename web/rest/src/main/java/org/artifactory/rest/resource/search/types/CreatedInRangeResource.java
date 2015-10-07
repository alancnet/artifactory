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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.common.RestDateFieldName;
import org.artifactory.api.rest.search.result.CreatedInRangeRestSearchResult;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.common.util.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.artifactory.api.rest.constant.SearchRestConstants.*;

/**
 * @author Eli Givoni
 */
public class CreatedInRangeResource extends GenericSearchResource {
    private static final Logger log = LoggerFactory.getLogger(CreatedInRangeResource.class);

    public CreatedInRangeResource(AuthorizationService authorizationService, SearchService searchService,
            HttpServletRequest request) {
        super(searchService, authorizationService, request);
    }

    /**
     * Returns a json array of modified artifacts within the time range in the format:
     * <pre>
     * [{
     *    "url" : "http://.../libs-releases-local/path_to_artifact",
     *    "modified" : "2009-07-02T11:11:49+03:00"
     *  } ...]
     * </pre>
     * Where the url is the full url of the modified artifact and the modified is the created or modified date of the
     * artifact that is in within the time range.
     *
     * @param from          The time to start the search. Exclusive (eg, >). If empty will start from 1st Jan 1970
     * @param to            The time to end search. Inclusive (eg, <=), If empty, will not use current time as the
     *                      limit
     * @param reposToSearch Lists of repositories to search within
     * @return CreatedInRangeRestSearchResult Json representation of the modified artifacts whiting the time range
     */
    @GET
    @Produces({SearchRestConstants.MT_CREATED_IN_RANGE_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public Response get(@QueryParam(PARAM_IN_RANGE_FROM) Long from, @QueryParam(PARAM_IN_RANGE_TO) Long to,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch) throws IOException {
        RestDateFieldName[] dateFields = {RestDateFieldName.CREATED, RestDateFieldName.LAST_MODIFIED};
        return search(from, to, reposToSearch, dateFields);
    }

    @Override
    protected Response createResponse(ItemSearchResults<ArtifactSearchResult> results, Calendar from, Calendar to,
            RestDateFieldName[] dates) {
        CreatedInRangeRestSearchResult rangeRestSearchResult = new CreatedInRangeRestSearchResult();
        for (ArtifactSearchResult result : results.getResults()) {
            ItemInfo item = result.getItemInfo();

            // Find if modified or created is the date that was used to find this artifact
            Calendar dateFound = Calendar.getInstance();
            dateFound.setTimeInMillis(item.getCreated());
            if (to != null && !(dateFound.before(to) || dateFound.equals(to))) {
                dateFound.setTimeInMillis(item.getLastModified());
            } else if (from != null && !(dateFound.after(from) || dateFound.equals(from))) {
                // if created not in range then the last modified is
                dateFound.setTimeInMillis(item.getLastModified());
            }

            String time = RestUtils.toIsoDateString(dateFound.getTimeInMillis());
            CreatedInRangeRestSearchResult.CreatedEntry entry =
                    new CreatedInRangeRestSearchResult.CreatedEntry(buildStorageUri(item.getRepoPath()), time);
            rangeRestSearchResult.results.add(entry);
        }
        return Response.ok(rangeRestSearchResult).type(MT_CREATED_IN_RANGE_SEARCH_RESULT).build();
    }
}
