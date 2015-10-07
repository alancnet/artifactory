package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Aviad Shikloshi
 */
public abstract class RepositoryInfo implements RestModel {

    protected String repoKey;
    protected String repoType;
    protected Boolean hasReindexAction;

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public Boolean getHasReindexAction() {
        return hasReindexAction;
    }

    public void setHasReindexAction(Boolean hasReindexAction) {
        this.hasReindexAction = hasReindexAction;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }


}
