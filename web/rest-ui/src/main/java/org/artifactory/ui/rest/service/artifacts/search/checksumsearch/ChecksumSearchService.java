package org.artifactory.ui.rest.service.artifacts.search.checksumsearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.checksumsearch.ChecksumSearch;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearchResult;
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
public class ChecksumSearchService implements RestService {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ChecksumSearch checksumSearch = (ChecksumSearch) request.getImodel();
        ChecksumSearchControls checksumSearchControl = getChecksumSearchControl(checksumSearch);
        if (isSearchEmptyOrWildCardOnly(checksumSearchControl)) {
            response.error("Please enter a valid checksum to search for");
            return;
        }
        // search checksum artifact
        ItemSearchResults<ArtifactSearchResult> checksumResults = searchService.getArtifactsByChecksumResults(
                checksumSearchControl);
        // update model search
        List<QuickSearchResult> checksumResultList = updateSearchModels(checksumResults);
        int maxResults = ConstantValues.searchMaxResults.getInt();
        long resultsCount;
        if (checksumSearchControl.isLimitSearchResults() && checksumResultList.size() > maxResults) {
            checksumResultList = checksumResultList.subList(0, maxResults);
            resultsCount = checksumResultList.size() == 0 ? 0 : checksumResults.getFullResultsCount();
        } else {
            resultsCount = checksumResultList.size();
        }
        // update response
        SearchResult model = new SearchResult(checksumResultList, checksumSearch.getChecksum(),
                resultsCount, checksumSearchControl.isLimitSearchResults());
        model.addNotifications(response);
        response.iModel(model);
    }

    /**
     * update search model with results
     *
     * @param checksumResults - checksum search result
     * @return list of search models
     */
    private List<QuickSearchResult> updateSearchModels(ItemSearchResults<ArtifactSearchResult> checksumResults) {
        List<QuickSearchResult> checksumResultList = new ArrayList<>();
        checksumResults.getResults().stream()
                .filter(this::filterNoReadResults)
                .forEach(checksumResult -> checksumResultList.add(new QuickSearchResult(checksumResult)));
        return checksumResultList;
    }

    private boolean filterNoReadResults(ArtifactSearchResult checksumResult) {
        RepoPath repoPath = RepoPathFactory.create(checksumResult.getRepoKey(), checksumResult.getRelativePath());
        return authorizationService.canRead(repoPath);
    }


    /**
     * check if search is empty or contain wildcard only
     *
     * @param checksumSearchControls
     * @return if true - search is empty or containing wild card only
     */
    private boolean isSearchEmptyOrWildCardOnly(ChecksumSearchControls checksumSearchControls) {
        return checksumSearchControls.isEmpty() || checksumSearchControls.isWildcardsOnly();
    }

    /**
     * create checksum search control
     *
     * @return Checksum search controls
     */
    private ChecksumSearchControls getChecksumSearchControl(ChecksumSearch checksumSearch) {
        String query = checksumSearch.getChecksum();
        ChecksumSearchControls searchControls = new ChecksumSearchControls();
        if (StringUtils.isNotBlank(query)) {
            if (StringUtils.length(query) == ChecksumType.md5.length()) {
                searchControls.addChecksum(ChecksumType.md5, query);
                searchControls.setLimitSearchResults(true);
            } else if (StringUtils.length(query) == ChecksumType.sha1.length()) {
                searchControls.addChecksum(ChecksumType.sha1, query);
            }
            searchControls.setSelectedRepoForSearch(checksumSearch.getSelectedRepositories());
        }
        return searchControls;
    }

}
