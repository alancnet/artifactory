package org.artifactory.rest.services;

import org.artifactory.rest.services.replication.CreateMultipleReplicationService;
import org.artifactory.rest.services.replication.CreateReplicationService;
import org.artifactory.rest.services.replication.DeleteReplicationsService;
import org.artifactory.rest.services.replication.GetReplicationService;
import org.artifactory.rest.services.replication.UpdateMultipleReplicationsService;
import org.artifactory.rest.services.replication.UpdateReplicationService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class ConfigServiceFactory {

    // get replication services
    @Lookup
    public abstract GetReplicationService getReplication();

    @Lookup
    public abstract CreateReplicationService createOrReplaceReplication();

    @Lookup
    public abstract CreateMultipleReplicationService createMultipleReplication();

    @Lookup
    public abstract UpdateReplicationService updateReplication();

    @Lookup
    public abstract UpdateMultipleReplicationsService updateMultipleReplications();

    @Lookup
    public abstract DeleteReplicationsService deleteReplicationsService();


}
