package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;

/**
 * @author Dan Feldman
 */
public class PypiTypeSpecificConfigModel implements TypeSpecificConfigModel {

    //remote
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Pypi;
    }

    @Override
    public String getUrl() {
        return RepoConfigDefaultValues.PYPI_URL;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
