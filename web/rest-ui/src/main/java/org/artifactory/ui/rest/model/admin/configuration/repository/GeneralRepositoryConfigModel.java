package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class GeneralRepositoryConfigModel implements RestModel {

    protected String repoKey;

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
