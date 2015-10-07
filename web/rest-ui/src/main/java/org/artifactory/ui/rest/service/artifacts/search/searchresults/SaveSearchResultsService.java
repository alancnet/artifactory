package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.utils.RequestUtils;
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
public class SaveSearchResultsService extends BaseSearchResultService {

    @Autowired
    AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String searchName = request.getQueryParamByKey("name");
        boolean useVersionLevel = Boolean.valueOf(request.getQueryParamByKey("useVersion"));
        List<ItemSearchResult> results = new ArrayList<>();
        List<BaseSearchResult> baseSearchResults = (List<BaseSearchResult>) request.getModels();
        SavedSearchResults searchResults = getSavedSearchResults(searchName, results, baseSearchResults,
                useVersionLevel);
        // save search results to session
        RequestUtils.setResultsToRequest(searchResults, request.getServletRequest());
        response.info("Search results successfully saved to stash");
    }


}
