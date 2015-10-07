package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.RestPaging;

/**
 * @author Chen Keinan
 */
public class PagingModel extends BaseModel {

    PagingModel() {
    }

    private String totalItems;

    private List<? extends RestPaging> pagingData;

    public PagingModel(
            long numOfPages, List<? extends RestPaging> pagingData) {
        this.totalItems = Long.toString(numOfPages);
        this.pagingData = pagingData;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public List<? extends RestPaging> getPagingData() {
        return pagingData;

    }

    public void setPagingData(
            List<EffectivePermissionsArtifactInfo> pagingData) {
        this.pagingData = pagingData;
    }
}
