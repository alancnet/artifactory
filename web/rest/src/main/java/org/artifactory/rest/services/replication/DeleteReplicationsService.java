package org.artifactory.rest.services.replication;

import java.util.List;

import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteReplicationsService extends BaseReplicationService {
    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        String url = artifactoryRequest.getQueryParamByKey("url");
        verifyArtifactoryPro();
        verifyRepositoryExists(repoKey);
        MutableCentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        switch (RestUtils.repoType(repoKey)) {
            case LOCAL:
                // delete local replications
                deleteLocalReplications(repoKey, url, descriptor);
                break;
            case REMOTE:
                // delete remote replications
                deleteRemoteReplication(repoKey, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
    }

    /**
     * delete local replications
     *
     * @param repoKey    - repo key
     * @param url        - url
     * @param descriptor - config descriptor
     */
    private void deleteLocalReplications(String repoKey, String url, MutableCentralConfigDescriptor descriptor) {
        if (url.length() == 0) {
            deleteAllReplication(repoKey, descriptor);
        } else {
            deleteSpecificReplication(repoKey, url, descriptor);
        }
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }

    /**
     * iterate repo replication and delete replication with same kkey and url
     *
     * @param repoKey    - repo key
     * @param url        - url
     * @param descriptor - config descriptor
     */
    private void deleteSpecificReplication(String repoKey, String url, MutableCentralConfigDescriptor descriptor) {
        List<LocalReplicationDescriptor> localReplications = descriptor.getMultiLocalReplications(repoKey);
        if (localReplications == null || localReplications.isEmpty()) {
            throw new BadRequestException("Could not find existing replication for delete");
        }
        int replicationIndex = 0;
        for (LocalReplicationDescriptor localReplicationDescriptor : localReplications) {
            if (localReplicationDescriptor.getRepoKey().equals(repoKey) && localReplicationDescriptor.getUrl().equals(
                    url)) {
                break;
            }
            replicationIndex = replicationIndex + 1;
        }
        if (localReplications.size() == replicationIndex) {
            throw new BadRequestException("Invalid replication url");
        } else {
            localReplications.remove(replicationIndex);
            descriptor.setLocalReplications(localReplications);
        }
    }

    /**
     * replication and delete all replication for repo key
     *
     * @param repoKey    - repo key
     * @param descriptor - config descriptor
     */
    private void deleteAllReplication(String repoKey, CentralConfigDescriptor descriptor) {
        List<LocalReplicationDescriptor> localReplications = descriptor.getMultiLocalReplications(repoKey);
        if (localReplications == null || localReplications.isEmpty()) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        descriptor.getLocalReplications().removeAll(localReplications);
    }

    /**
     * delete remote replications
     *
     * @param repoKey    - repo key
     * @param descriptor - config descriptor
     */
    private void deleteRemoteReplication(String repoKey, CentralConfigDescriptor descriptor) {
        RemoteReplicationDescriptor remoteReplication = descriptor.getRemoteReplication(repoKey);
        if (remoteReplication == null) {
            throw new BadRequestException("Could not find existing replication for update");
        }
        descriptor.getRemoteReplications().remove(remoteReplication);
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }
}
