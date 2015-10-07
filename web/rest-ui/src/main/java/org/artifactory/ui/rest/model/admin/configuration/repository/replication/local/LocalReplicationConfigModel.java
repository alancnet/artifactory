package org.artifactory.ui.rest.model.admin.configuration.repository.replication.local;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryNetworkConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.RepositoryReplicationConfigModel;
import org.codehaus.jackson.annotate.JsonTypeName;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
@JsonTypeName("local")
public class LocalReplicationConfigModel extends RepositoryNetworkConfigModel implements RepositoryReplicationConfigModel {

    protected Boolean enabled = DEFAULT_LOCAL_REPLICATION_ENABLED;
    protected String cronExp;
    protected Boolean enableEventReplication = DEFAULT_EVENT_REPLICATION;
    protected String pathPrefix;
    protected Boolean syncDeletes = DEFAULT_REPLICATION_SYNC_DELETES;

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

    public Boolean isEnableEventReplication() {
        return enableEventReplication;
    }

    public void setEnableEventReplication(Boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
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
    public Boolean isSyncDeletes() {
        return syncDeletes;
    }

    @Override
    public void setSyncDeletes(Boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
