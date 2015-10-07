package org.artifactory.rest.services.replication;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.MissingRestAddonException;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.repo.Repo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.services.IService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Chen Keinan
 */
public abstract class BaseReplicationService implements IService {

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    protected CentralConfigService centralConfigService;

    protected void verifyArtifactoryPro() {
        if (addonsManager.addonByType(RestAddon.class).isDefault()) {
            throw new MissingRestAddonException();
        }
    }

    protected Repo verifyRepositoryExists(String repoKey) {
        Repo repo = repositoryByKey(repoKey);
        if (repo == null) {
            throw new BadRequestException("Could not find repository");
        }
        return repo;
    }

    protected Repo repositoryByKey(String repoKey) {
        return ((InternalRepositoryService) ContextHelper.get().getRepositoryService()).repositoryByKey(repoKey);
    }

    /**
     * add or replace replication
     *
     * @param newReplication - new replication
     * @param replications   - list of replications
     * @param <T>            - new replication
     */
    protected <T extends ReplicationBaseDescriptor> void addOrReplace(T newReplication, List<T> replications) {
        String repoKey = newReplication.getRepoKey();
        T existingReplication = getReplication(repoKey, replications);
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
    protected <T extends ReplicationBaseDescriptor> T getReplication(String replicatedRepoKey, List<T> replications) {
        for (T replication : replications) {
            if (replicatedRepoKey.equals(replication.getRepoKey())) {
                return replication;
            }
        }
        return null;
    }
}
