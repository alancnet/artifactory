package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.fs.FileInfo;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.search.StashResult;
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
public class GetSearchResultsService extends BaseSearchResultService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String searchName = request.getQueryParamByKey("name");
        SavedSearchResults resultsToRequest = RequestUtils.getResultsFromRequest(searchName,
                request.getServletRequest());
        if (resultsToRequest != null) {
            // remove items which has been deleted from save search result
            removeNonValidDataFromResult(resultsToRequest);
            List<StashResult> stashResults = new ArrayList<>();
            resultsToRequest.getResults().forEach(result ->
                    stashResults.add(new StashResult(result.getName(), result.getRelPath(), result.getRepoKey())));
            response.iModelList(stashResults);
        }
    }

    /**
     * remove items which has been deleted from save search result
     *
     * @param resultsToRequest - results from session
     */
    private void removeNonValidDataFromResult(SavedSearchResults resultsToRequest) {
        ImmutableList.Builder<FileInfo> builder = ImmutableList.builder();
        resultsToRequest.getResults().forEach(result -> {
            try {
                repositoryService.getItemInfo(result.getRepoPath());
            } catch (ItemNotFoundRuntimeException e) {
                builder.add(result);
            }
        });
        ImmutableList<FileInfo> infos = builder.build();
        resultsToRequest.removeAll(infos);
    }
}
