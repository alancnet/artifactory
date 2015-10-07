package org.artifactory.ui.rest.service.artifacts.search.gavcsearch;

import com.google.common.collect.Lists;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.gavc.GavcSearchControls;
import org.artifactory.api.search.gavc.GavcSearchResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.gavcsearch.GavcResult;
import org.artifactory.ui.rest.model.artifacts.search.gavcsearch.GavcSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GavcSearchService implements RestService {

    @Autowired
    private SearchService searchService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        GavcSearch gavcSearch = (GavcSearch) request.getImodel();
        GavcSearchControls gavcSearchControls = getGavcSearchControls(gavcSearch);

        if (isSearchEmptyOrWildCardOnly(gavcSearchControls)) {
            response.error("Search term empty or containing only wildcards is not permitted");
            return;
        }
        // search gavc
        ItemSearchResults<GavcSearchResult> searchControls = search(gavcSearchControls);
        List<GavcResult> gavcResults = Lists.newArrayList();
        for (GavcSearchResult gavcSearchResult : searchControls.getResults()) {
            gavcResults.add(new GavcResult(gavcSearchResult));
        }
        int maxResults = ConstantValues.searchMaxResults.getInt();
        long resultsCount;
        if (gavcSearchControls.isLimitSearchResults() && gavcResults.size() > maxResults) {
            gavcResults = gavcResults.subList(0, maxResults);
            resultsCount = gavcResults.size() == 0 ? 0 : searchControls.getFullResultsCount();
        } else {
            resultsCount = gavcResults.size();
        }

        // update response data
        SearchResult model = new SearchResult(gavcResults, gavcSearchControls.getSearchExpression(),
                resultsCount, gavcSearchControls.isLimitSearchResults());
        model.addNotifications(response);
        response.iModel(model);
    }

    /**
     * check if search is empty or contain wildcard only
     *
     * @param gavcSearchControls
     * @return if true - search is empty or containing wild card only
     */
    private boolean isSearchEmptyOrWildCardOnly(GavcSearchControls gavcSearchControls) {
        return gavcSearchControls.isEmpty() || gavcSearchControls.isWildcardsOnly();
    }

    /**
     * get gavc searcch controls
     *
     * @param gavcSearch - gavc search model
     * @return - gave cearch control inctance
     */
    private GavcSearchControls getGavcSearchControls(GavcSearch gavcSearch) {
        GavcSearchControls gavcSearchControls = new GavcSearchControls();
        gavcSearchControls.setArtifactId(gavcSearch.getArtifactID());
        gavcSearchControls.setGroupId(gavcSearch.getGroupID());
        gavcSearchControls.setClassifier(gavcSearch.getClassifier());
        gavcSearchControls.setVersion(gavcSearch.getVersion());
        gavcSearchControls.setLimitSearchResults(true);
        gavcSearchControls.setSelectedRepoForSearch(gavcSearch.getSelectedRepositories());
        return gavcSearchControls;
    }

    /**
     * Performs the search
     *
     * @param controls Search controls
     * @return List of search results
     */
    private ItemSearchResults<GavcSearchResult> search(GavcSearchControls controls) {
        return searchService.searchGavc(controls);
    }


}
