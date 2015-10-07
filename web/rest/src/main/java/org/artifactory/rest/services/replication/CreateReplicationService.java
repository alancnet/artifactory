package org.artifactory.rest.services.replication;

import javax.servlet.http.HttpServletResponse;

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
public class CreateReplicationService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        ReplicationConfigRequest replicationRequest = (ReplicationConfigRequest) artifactoryRequest.getImodel();
        // verify pro license
        verifyArtifactoryPro();
        // verify repo key
        verifyRepositoryExists(repoKey);
        CentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        switch (RestUtils.repoType(repoKey)) {
            case LOCAL:
                // add or replace local replication
                addOrReplaceLocalReplication(repoKey, replicationRequest, descriptor);
                break;
            case REMOTE:
                // add or replace remote replication
                addOrReplaceRemoteReplication(repoKey, replicationRequest, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
        // update response code
        artifactoryResponse.setResponseCode(HttpServletResponse.SC_CREATED);
    }

    /**
     * add or replace remote replication
     *
     * @param repoKey            - repository key
     * @param replicationRequest - replication request model
     * @param descriptor         - config descriptor
     */
    private void addOrReplaceRemoteReplication(String repoKey, ReplicationConfigRequest replicationRequest,
            CentralConfigDescriptor descriptor) {
        RemoteReplicationDescriptor remoteReplication = new RemoteReplicationDescriptor();
        remoteReplication.setRepoKey(repoKey);
        ReplicationConfigRequestHelper.fillBaseReplicationDescriptor(replicationRequest, remoteReplication);
        ReplicationConfigRequestHelper.verifyBaseReplicationRequest(remoteReplication);
        addOrReplace(remoteReplication, descriptor.getRemoteReplications());
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * add or replace local replication
     *
     * @param repoKey            - repository key
     * @param replicationRequest - replication request model
     * @param descriptor         - config descriptor
     */
    private void addOrReplaceLocalReplication(String repoKey, ReplicationConfigRequest replicationRequest,
            CentralConfigDescriptor descriptor) {
        LocalReplicationDescriptor localReplication = new LocalReplicationDescriptor();
        localReplication.setRepoKey(repoKey);
        ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replicationRequest, localReplication);
        ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
        addOrReplace(localReplication, descriptor.getLocalReplications());
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
