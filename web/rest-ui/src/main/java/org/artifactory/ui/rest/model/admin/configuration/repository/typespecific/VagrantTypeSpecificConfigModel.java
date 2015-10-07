package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Dan Feldman
 */
public class VagrantTypeSpecificConfigModel implements TypeSpecificConfigModel {

    @Override
    public RepoType getRepoType() {
        return RepoType.Vagrant;
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
