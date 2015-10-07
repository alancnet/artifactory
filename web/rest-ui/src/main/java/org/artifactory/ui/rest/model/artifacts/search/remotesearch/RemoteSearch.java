package org.artifactory.ui.rest.model.artifacts.search.remotesearch;

import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

/**
 * @author Chen Keinan
 */
public class RemoteSearch extends BaseSearch {

    private String searchKey;

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
}
