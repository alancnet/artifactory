package org.artifactory.rest.services.replication;

import java.util.List;

import org.artifactory.api.rest.replication.MultipleReplicationConfigRequest;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.artifactory.rest.resource.replication.ReplicationConfigRequestHelper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateMultipleReplicationsService extends BaseReplicationService {
    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        MultipleReplicationConfigRequest replicationRequest = (MultipleReplicationConfigRequest) artifactoryRequest.getImodel();
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        // validate pro liccense
        verifyArtifactoryPro();
        // validate repo key
        verifyRepositoryExists(repoKey);
        CentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        switch (RestUtils.repoType(repoKey)) {
            case LOCAL:
                // update Local replications
                updateMultipleLocalReplication(replicationRequest, repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
    }

    /**
     * update local replications
     *
     * @param replicationRequest - replications request model
     * @param repoKey            - repository key
     * @param descriptor         - config descriptor
     */
    private void updateMultipleLocalReplication(MultipleReplicationConfigRequest replicationRequest, String repoKey,
            CentralConfigDescriptor descriptor) {
        String cronExp = replicationRequest.getCronExp();
        Boolean eventReplications = replicationRequest.isEnableEventReplication();
        List<ReplicationConfigRequest> replications = replicationRequest.getReplications();
        for (ReplicationConfigRequest replication : replications) {
            LocalReplicationDescriptor localReplication = descriptor.getLocalReplication(repoKey, replication.getUrl());
            if (localReplication == null) {
                throw new BadRequestException("Could not find existing replication for update");
            }
            if (eventReplications != null) {
                localReplication.setEnableEventReplication(eventReplications);
            }
            if (cronExp != null) {
                localReplication.setCronExp(cronExp);
            }
            ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replication, localReplication);
            ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
        }
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
