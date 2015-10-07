package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE;

/**
 * @author Dan Feldman
 */
public class GenericTypeSpecificConfigModel implements TypeSpecificConfigModel {

    //remote
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE;

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Generic;
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
