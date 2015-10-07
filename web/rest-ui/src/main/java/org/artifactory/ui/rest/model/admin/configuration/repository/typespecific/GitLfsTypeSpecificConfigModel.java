package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Dan Feldman
 */
public class GitLfsTypeSpecificConfigModel implements TypeSpecificConfigModel {

    @Override
    public RepoType getRepoType() {
        return RepoType.GitLfs;
    }

    @Override
    public String getUrl() {
        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
