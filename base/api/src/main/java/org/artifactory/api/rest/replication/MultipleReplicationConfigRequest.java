package org.artifactory.api.rest.replication;

import org.artifactory.api.rest.restmodel.IModel;

import java.io.Serializable;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class MultipleReplicationConfigRequest implements Serializable, IModel {

    private String cronExp;
    private Boolean enableEventReplication;
    private List<ReplicationConfigRequest> replications;

    public List<ReplicationConfigRequest> getReplications() {
        return replications;
    }

    public void setReplications(
            List<ReplicationConfigRequest> replications) {
        this.replications = replications;
    }

    public String getCronExp() {
        return cronExp;
    }

    public void setCronExp(String cronExp) {
        this.cronExp = cronExp;
    }

    public Boolean isEnableEventReplication() {
        return enableEventReplication;
    }

    public void setEnableEventReplication(Boolean enableEventReplication) {
        this.enableEventReplication = enableEventReplication;
    }
}
