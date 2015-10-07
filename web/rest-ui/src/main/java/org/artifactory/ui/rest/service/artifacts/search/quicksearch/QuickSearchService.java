package org.artifactory.ui.rest.service.artifacts.search.quicksearch;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearch;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearchResult;
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
public class QuickSearchService implements RestService {

    @Autowired
    private SearchService searchService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        QuickSearch quickSearch = (QuickSearch) request.getImodel();
        // get artifact search control instance
        ArtifactSearchControls artifactSearchControl = getArtifactSearchControl(quickSearch);
        // check if search empty or contain wild card only
        if (isSearchEmptyOrWildCardOnly(artifactSearchControl)) {
            response.error("Search term empty or containing only wildcards is not permitted");
            return;
        }
        // do quick search
        List<QuickSearchResult> quickSearchResults = Lists.newArrayList();
        ItemSearchResults<ArtifactSearchResult> artifactSearchResults = searchService.searchArtifacts(artifactSearchControl);
        for (ArtifactSearchResult artifactSearchResult : artifactSearchResults.getResults()) {
            quickSearchResults.add(new QuickSearchResult(artifactSearchResult));
        }
        long resultsCount;
        int maxResults = ConstantValues.searchMaxResults.getInt();
        if (artifactSearchControl.isLimitSearchResults() && quickSearchResults.size() > maxResults) {
            quickSearchResults = quickSearchResults.subList(0, maxResults);
            resultsCount = quickSearchResults.size() == 0 ? 0 : artifactSearchResults.getFullResultsCount();
        }else{
            resultsCount = quickSearchResults.size();
        }
        SearchResult model = new SearchResult(quickSearchResults, quickSearch.getQuery(),
                resultsCount, artifactSearchControl.isLimitSearchResults());
        model.addNotifications(response);
        response.iModel(model);
    }


    /**
     * check if search is empty or contain wildcard only
     *
     * @param artifactSearchControl
     * @return if true - search is empty or containing wild card only
     */
    private boolean isSearchEmptyOrWildCardOnly(ArtifactSearchControls artifactSearchControl) {
        return artifactSearchControl.isEmpty() || artifactSearchControl.isWildcardsOnly();
    }

    /**
     * create artifact search control from quick search instance
     *
     * @param quickSearch - quick search instance
     * @return artifact search control instance
     */
    private ArtifactSearchControls getArtifactSearchControl(QuickSearch quickSearch) {
        ArtifactSearchControls artifactSearchControls = new ArtifactSearchControls();
        artifactSearchControls.setSelectedRepoForSearch(quickSearch.getSelectedRepositories());
        artifactSearchControls.setLimitSearchResults(!quickSearch.getSelectedRepositories().isEmpty());
        artifactSearchControls.setRelativePath(quickSearch.getRelativePath());
        artifactSearchControls.setQuery(quickSearch.getQuery());
        artifactSearchControls.setLimitSearchResults(true);
        return artifactSearchControls;
    }

    /**
     * add wild card to query
     *
     * @param artifactSearchControls - quick search model
     */
    private void setQueryWildCard(ArtifactSearchControls artifactSearchControls) {
        String query = artifactSearchControls.getQuery();
        if (StringUtils.isNotBlank(query)) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(query);

            if (!query.endsWith("*") && !query.endsWith("?")) {
                queryBuilder.append("*");
            }
            artifactSearchControls.setQuery(queryBuilder.toString());
        }
    }
}
