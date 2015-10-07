package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.ui.utils.RegExUtils;

/**
 * @author Aviad Shikloshi
 */
public class LocalRepositoryInfo extends RepositoryInfo {

    private Boolean replications;

    public LocalRepositoryInfo() {
    }

    public LocalRepositoryInfo(LocalRepoDescriptor repoDescriptor, CentralConfigService configService) {
        repoKey = repoDescriptor.getKey();
        repoType = repoDescriptor.getType().toString();
        // checks if there is at least one enabled replication
        replications = (configService.getDescriptor().getMultiLocalReplications(repoKey).stream()
                .filter(ReplicationBaseDescriptor::isEnabled).findFirst().isPresent());
        hasReindexAction = RegExUtils.LOCAL_REPO_REINDEX_PATTERN.matcher(repoType).matches();
    }

    public Boolean getReplications() {
        return replications;
    }

    public void setReplications(Boolean replications) {
        this.replications = replications;
    }
}
