package org.artifactory.ui.rest.model.artifacts.search.quicksearch;

import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;


/**
 * @author Chen Keinan
 */
public class QuickSearch extends BaseSearch {
    private String query;
    private String relativePath;
    private ItemSearchResults<ArtifactSearchResult> searchResultItemSearchResults;

    public QuickSearch() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public ItemSearchResults<ArtifactSearchResult> getSearchResultItemSearchResults() {
        return searchResultItemSearchResults;
    }

    public void setSearchResultItemSearchResults(
            ItemSearchResults<ArtifactSearchResult> searchResultItemSearchResults) {
        this.searchResultItemSearchResults = searchResultItemSearchResults;
    }
}
