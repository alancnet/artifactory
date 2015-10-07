package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.relatedrepositories;

import org.artifactory.api.repo.VirtualBrowsableItem;
import org.artifactory.descriptor.repo.RepoDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class RelatedRepositories {

    private List<String> relatedRepositories;

    public RelatedRepositories() {
    }

    public List<String> getRelatedRepositories() {
        return relatedRepositories;
    }

    public void setRelatedRepositories(List<String> relatedRepositories) {
        this.relatedRepositories = relatedRepositories;
    }

    /**
     * populate virtual repositories list
     */
    public void populateRelatedRepositories(VirtualBrowsableItem virtualBrowsableItem) {
        this.setRelatedRepositories(virtualBrowsableItem.getRepoKeys());
    }

    /**
     * populate virtual repositories list
     */
    public void populateRelatedRepositories(List<RepoDescriptor> repoDescriptors) {
        List<String> relatedRepositories = new ArrayList<>();
        repoDescriptors.forEach(repoDescriptor -> relatedRepositories.add(repoDescriptor.getKey()));
        this.setRelatedRepositories(relatedRepositories);
    }
}
