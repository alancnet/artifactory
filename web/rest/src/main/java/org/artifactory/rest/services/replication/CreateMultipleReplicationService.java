package org.artifactory.rest.services.replication;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.artifactory.api.rest.replication.MultipleReplicationConfigRequest;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
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
public class CreateMultipleReplicationService extends BaseReplicationService {

    @Override
    public void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse) {
        String repoKey = artifactoryRequest.getPathParamByKey("repoKey");
        MultipleReplicationConfigRequest replicationRequests = (MultipleReplicationConfigRequest) artifactoryRequest.getImodel();
        // verify pro license
        verifyArtifactoryPro();
        // verify repo key
        verifyRepositoryExists(repoKey);
        CentralConfigDescriptor descriptor = centralConfigService.getMutableDescriptor();
        switch (RestUtils.repoType(repoKey)) {
            case LOCAL:
                // add or replace local replication
                addOrReplaceLocalReplication(repoKey, replicationRequests, descriptor);
                break;
            default:
                throw new BadRequestException("Invalid repository");
        }
        // update response code
        artifactoryResponse.setResponseCode(HttpServletResponse.SC_CREATED);
    }

    /**
     * add or replace local replication
     *
     * @param repoKey            - repository key
     * @param replicationRequest - replication request model
     * @param descriptor         - config descriptor
     */
    private void addOrReplaceLocalReplication(String repoKey, MultipleReplicationConfigRequest replicationRequest,
            CentralConfigDescriptor descriptor) {
        String cronExp = replicationRequest.getCronExp();
        Boolean eventReplication = replicationRequest.isEnableEventReplication();
        List<ReplicationConfigRequest> replicationConfigRequests = replicationRequest.getReplications();
        for (ReplicationConfigRequest replicationConfigRequest : replicationConfigRequests) {
            LocalReplicationDescriptor localReplication = new LocalReplicationDescriptor();
            localReplication.setRepoKey(repoKey);
            // set cron and event for all
            if (eventReplication != null) {
                replicationConfigRequest.setEnableEventReplication(eventReplication);
            }
            if (cronExp != null) {
                replicationConfigRequest.setCronExp(cronExp);
            }
            ReplicationConfigRequestHelper.fillLocalReplicationDescriptor(replicationConfigRequest, localReplication);
            ReplicationConfigRequestHelper.verifyLocalReplicationRequest(localReplication);
            addOrReplace(localReplication, descriptor.getLocalReplications());
        }
        centralConfigService.saveEditedDescriptorAndReload(descriptor);
    }


    /**
     * add or replace replication
     *
     * @param newReplication - new replication
     * @param replications   - list of replications
     * @param <T>            - new replication
     */
    protected <T extends LocalReplicationDescriptor> void addOrReplace(T newReplication, List<T> replications) {
        String repoKey = newReplication.getRepoKey();
        String url = newReplication.getUrl();
        T existingReplication = getReplication(repoKey, replications, url);
        if (existingReplication != null) {
            int i = replications.indexOf(existingReplication);
            replications.set(i, newReplication); //replace
        } else {
            replications.add(newReplication); //add
        }
    }


    /**
     * get replication
     *
     * @param <T>               replication base descriptor
     * @param replicatedRepoKey - replication repo key
     * @param replications      - list of replications
     * @param url
     * @return replication descriptor
     */
    protected <T extends ReplicationBaseDescriptor> T getReplication(String replicatedRepoKey, List<T> replications,
            String url) {
        for (T replication : replications) {
            if (replicatedRepoKey.equals(
                    replication.getRepoKey()) && ((LocalReplicationDescriptor) replication).getUrl().equals(url)) {
                return replication;
            }
        }
        return null;
    }
}
