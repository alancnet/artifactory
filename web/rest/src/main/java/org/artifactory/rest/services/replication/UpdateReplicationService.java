package org.artifactory.rest.services.replication;

import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
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
public class UpdateReplicationService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        ReplicationConfigRequest replicationRequest = (ReplicationConfigRequest) artifactoryRequest.getImodel();
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        // validate pro liccense
        verifyArtifactoryPro();
        // validate repo key
        verifyRepositoryExists(repoKey);
        CentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        switch (RestUtils.repoType(repoKey)) {
            case LOCAL:
                // update Local replications
                updateLocalReplication(replicationRequest, repoKey, descriptor);
                break;
            case REMOTE:
                // update remote replications
                updateRemoteReplications(replicationRequest, repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
    }

    /**
     * update remote replications
     *
     * @param replicationRequest - replication  request model
     * @param repoKey            - rep key
     * @param descriptor         - config descriptor
     */
    private void updateRemoteReplications(ReplicationConfigRequest replicationRequest, String repoKey,
            CentralConfigDescriptor descriptor) {
        RemoteReplicationDescriptor remoteReplication = descriptor.getRemoteReplication(repoKey);
        if (remoteReplication == null) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        ReplicationConfigRequestHelper.fillBaseReplicationDescriptor(replicationRequest, remoteReplication);
        ReplicationConfigRequestHelper.verifyBaseReplicationRequest(remoteReplication);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * update local replications
     *
     * @param replicationRequest - replications request model
     * @param repoKey            - repository key
     * @param descriptor         - config descriptor
     */
    private void updateLocalReplication(ReplicationConfigRequest replicationRequest, String repoKey,
            CentralConfigDescriptor descriptor) {
        LocalReplicationDescriptor localReplication = descriptor.getLocalReplication(repoKey);
        if (localReplication == null) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replicationRequest, localReplication);
        ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
