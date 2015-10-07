package org.artifactory.ui.rest.model.artifacts.search;

import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.service.RestResponse;

/**
 * @author Shay Yaakov
 */
public class SearchResult extends BaseModel {

    private Object results;
    private String searchExpression;
    private long resultsCount;
    private boolean isLimitSearchResults;

    public SearchResult(Object results, String searchExpression, long resultsCount, boolean isLimitSearchResults) {
        this.results = results;
        this.searchExpression = searchExpression;
        this.resultsCount = resultsCount;
        this.isLimitSearchResults = isLimitSearchResults;
    }

    public Object getResults() {
        return results;
    }

    public String getMessage() {
        int maxResults = ConstantValues.searchMaxResults.getInt();
        int queryLimit = ConstantValues.searchUserQueryLimit.getInt();

        StringBuilder msg = new StringBuilder();
        //Return this only if we limit the search results and don't return the full number of results found
        if (isLimitSearchResults && resultsCount > maxResults) {
            msg.append("Showing first ").append(maxResults).append(" out of ").
                    append(resultsCount == queryLimit ? "more than " : "")
                    .append(resultsCount).append(" matches found");
        } else if (isLimitSearchResults && resultsCount == -1) {
            msg.append("Showing first ").append(maxResults).append(" found matches");
        } else {
            msg.append("Search Results (").append(resultsCount).append(" Items)");
        }
        return msg.toString();
    }

    public void addNotifications(RestResponse response) {
        if (resultsCount == 0) {
            response.warn("No artifacts found. You can broaden your search by using the * and ? wildcards");
        }
        if (isLimitSearchResults && resultsCount >= ConstantValues.searchMaxResults.getInt()) {
            response.warn("Search results are limited. Please consider refining your search.");
        }
    }
}
