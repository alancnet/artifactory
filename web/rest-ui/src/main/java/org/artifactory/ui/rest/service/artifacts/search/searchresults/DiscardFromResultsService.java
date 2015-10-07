package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearchResult;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DiscardFromResultsService extends BaseSearchResultService {
    private static final Logger log = LoggerFactory.getLogger(RemoveSearchResultsService.class);

    @Autowired
    RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        String path = request.getQueryParamByKey("path");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        String resultName = request.getQueryParamByKey("name");
        // get save search results from session
        SavedSearchResults savedSearchResults = RequestUtils.getResultsFromRequest(resultName,
                request.getServletRequest());
        SavedSearchResults resultsToDiscard = getResultsToDiscard(repoPath, resultName, savedSearchResults);
        //discard results from session
        savedSearchResults.discardFromResult(resultsToDiscard);
    }

    /**
     * get save search results to discard
     *
     * @param repoPath           - repo path
     * @param resultName         - result name
     * @param savedSearchResults - saved search result instance
     * @return
     */
    private SavedSearchResults getResultsToDiscard(RepoPath repoPath, String resultName,
            SavedSearchResults savedSearchResults) {
        ImmutableList<FileInfo> results = savedSearchResults.getResults();
        List<BaseSearchResult> baseSearchResults = getResultsToDiscard(repoPath, results);
        List<ItemSearchResult> newResults = new ArrayList<>();
        return getSavedSearchResults(resultName, newResults, baseSearchResults, false);
    }

    /**
     * get results to discard
     *
     * @param repoPath - repo path
     * @param results  - results
     * @return - list of saved search result to discard
     */
    private List<BaseSearchResult> getResultsToDiscard(RepoPath repoPath, ImmutableList<FileInfo> results) {
        List<BaseSearchResult> baseSearchResults = new ArrayList<>();
        results.forEach(result -> {
            if (result.getRepoPath().toString().startsWith(repoPath.toString())) {
                QuickSearchResult quickSearchResult = new QuickSearchResult();
                quickSearchResult.setRepoKey(result.getRepoKey());
                quickSearchResult.setRelativePath(result.getRelPath());
                baseSearchResults.add(quickSearchResult);
            }
        });
        return baseSearchResults;
    }
}
