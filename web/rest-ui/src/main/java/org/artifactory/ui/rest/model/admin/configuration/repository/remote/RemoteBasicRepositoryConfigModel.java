package org.artifactory.ui.rest.model.admin.configuration.repository.remote;

import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_DELEGATION_CONTEXT;
import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_OFFLINE;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
public class RemoteBasicRepositoryConfigModel extends LocalBasicRepositoryConfigModel {

    protected String url;
    protected String remoteLayoutMapping;
    protected Boolean offline = DEFAULT_OFFLINE;
    protected ContentSynchronisation contentSynchronisation = DEFAULT_DELEGATION_CONTEXT;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRemoteLayoutMapping() {
        return remoteLayoutMapping;
    }

    public void setRemoteLayoutMapping(String remoteLayoutMapping) {
        this.remoteLayoutMapping = remoteLayoutMapping;
    }

    public Boolean isOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }


    public ContentSynchronisation getContentSynchronisation() {
        return contentSynchronisation;
    }

    public void setContentSynchronisation(ContentSynchronisation contentSynchronisation) {
        this.contentSynchronisation = contentSynchronisation;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
