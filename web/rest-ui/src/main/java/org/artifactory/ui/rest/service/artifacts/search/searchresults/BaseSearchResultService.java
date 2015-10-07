package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import com.google.common.collect.Sets;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.search.SearchTreeBuilder;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.utils.RequestUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Chen keinan
 */
public abstract class BaseSearchResultService implements RestService {


    /**
     * get saved search results
     *
     * @param searchName        - result name
     * @param results           - search results
     * @param baseSearchResults - base
     * @param useVersionLevel
     * @return
     */
    protected SavedSearchResults getSavedSearchResults(String searchName, List<ItemSearchResult> results,
            List<BaseSearchResult> baseSearchResults, boolean useVersionLevel) {
        baseSearchResults.forEach(result -> results.add(result.getSearchResult()));
        return SearchTreeBuilder.buildFullArtifactsList(searchName, results, useVersionLevel);
    }

    /**
     * update error response
     *
     * @param response - encapsulate data related to response
     * @param status   - status
     */
    protected void updateErrorResponse(RestResponse response, MoveMultiStatusHolder status) {
        response.responseCode(HttpServletResponse.SC_CONFLICT);
        List<String> errors = new ArrayList<>();
        status.getErrors().forEach(error -> errors.add(error.getMessage()));
        response.errors(errors);
    }

    /**
     * fetch repo path set for move / copy
     *
     * @param request    - encapsulate data related to request
     * @param searchName - search name
     * @return - set of repo path
     */
    protected Set<RepoPath> getRepoPaths(ArtifactoryRestRequest request, String searchName) {
        SavedSearchResults savedSearchResults = RequestUtils.getResultsFromRequest(searchName,
                request.getServletRequest());
        Set<RepoPath> pathsToMove = Sets.newHashSet();
        savedSearchResults.getResults().forEach(result -> pathsToMove.add(result.getRepoPath()));
        //Move all results files
        return pathsToMove;
    }

}
