package org.artifactory.ui.rest.model.admin.configuration.repository.replication;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocalReplicationConfigModel.class, name = "localReplication"),
        @JsonSubTypes.Type(value = RemoteReplicationConfigModel.class, name = "remoteReplication")
})
public interface RepositoryReplicationConfigModel extends RestModel {

    Boolean isEnabled();

    void setEnabled(Boolean enabled);

    String getCronExp();

    void setCronExp(String cronExp);

    String getPathPrefix();

    void setPathPrefix(String pathPrefix);

    Boolean isSyncDeletes();

    void setSyncDeletes(Boolean syncDeletes);
}
