package org.artifactory.ui.rest.model.admin.configuration.repository.virtual;

import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;

/**
 * @author Shay Yaakov
 */
public class VirtualSelectedRepository {

    private String repoName;
    private String type; // local/remote/virtual

    public VirtualSelectedRepository() {
    }

    public VirtualSelectedRepository(RepoDescriptor descriptor) {
        this.repoName = descriptor.getKey();
        if (descriptor instanceof LocalRepoDescriptor) {
            this.type = "local";
        } else if (descriptor instanceof RemoteRepoDescriptor) {
            this.type = "remote";
        } else {
            this.type = "virtual";
        }
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
