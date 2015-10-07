package org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.codehaus.jackson.annotate.JsonTypeName;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeName("remote")
public class RemoteReplicationConfigModel implements RepositoryReplicationConfigModel {

    protected Boolean enabled = DEFAULT_REMOTE_REPLICATION_ENABLED;
    protected String cronExp;
    protected Boolean syncDeletes = DEFAULT_REPLICATION_SYNC_DELETES;
    protected Boolean syncProperties = DEFAULT_SYNC_PROPERTIES;
    protected String pathPrefix;

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getCronExp() {
        return cronExp;
    }

    @Override
    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    @Override
    public Boolean isSyncDeletes() {
        return syncDeletes;
    }

    @Override
    public void setSyncDeletes(Boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    public Boolean isSyncProperties() {
        return syncProperties;
    }

    public void setSyncProperties(Boolean syncProperties) {
        this.syncProperties = syncProperties;
    }

    @Override
    public String getPathPrefix() {
        return pathPrefix;
    }

    @Override
    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
