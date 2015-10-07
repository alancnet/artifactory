package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.ui.utils.RegExUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aviad Shikloshi
 */
public class VirtualRepositoryInfo extends RepositoryInfo {

    protected List<String> selectedRepos;
    private Integer numberOfIncludesRepositories;

    public VirtualRepositoryInfo() {
    }

    public VirtualRepositoryInfo(VirtualRepoDescriptor repoDescriptor) {
        repoKey = repoDescriptor.getKey();
        repoType = repoDescriptor.getType().toString();
        numberOfIncludesRepositories = repoDescriptor.getRepositories().size();
        selectedRepos = repoDescriptor.getRepositories().stream()
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
        hasReindexAction = RegExUtils.VIRTUAL_REPO_REINDEX_PATTERN.matcher(repoType).matches();
    }

    public Integer getNumberOfIncludesRepositories() {
        return numberOfIncludesRepositories;
    }

    public void setNumberOfIncludesRepositories(Integer numberOfIncludesRepositories) {
        this.numberOfIncludesRepositories = numberOfIncludesRepositories;
    }

    public List<String> getSelectedRepos() {
        return selectedRepos;
    }

    public void setSelectedRepos(List<String> selectedRepos) {
        this.selectedRepos = selectedRepos;
    }
}
