package org.artifactory.rest.common.service;

/**
 * @author Chen Keinan
 */
public class PagingData {

    private String orderBy;
    private String startOffset;
    private String limit;
    private String direction;

    public PagingData(ArtifactoryRestRequest restRequest) {
        this.orderBy = restRequest.getQueryParamByKey("orderBy");
        limit = restRequest.getQueryParamByKey("numOfRows");
        if (!limit.isEmpty()) {
            int numOfRows = Integer.parseInt(limit);
            int startRowNumber = ((Integer.parseInt(restRequest.getQueryParamByKey("pageNum")) - 1) * numOfRows);
            this.startOffset = new Integer(startRowNumber).toString();
        }
        this.direction = restRequest.getQueryParamByKey("direction");
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(String startOffset) {
        this.startOffset = startOffset;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
