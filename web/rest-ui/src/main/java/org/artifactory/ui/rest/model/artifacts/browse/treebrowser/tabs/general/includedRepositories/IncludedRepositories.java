package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.includedRepositories;

import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chen Keinan
 */
public class IncludedRepositories {

    private List<Repository> repositories = new ArrayList<>();

    public IncludedRepositories() {
    }

    public IncludedRepositories(List<RepoDescriptor> descriptors, HttpServletRequest request) {
        String baseUrl = HttpUtils.getServletContextUrl(request);
        String urlWithSlash = PathUtils.addTrailingSlash(baseUrl);
        descriptors.forEach(descriptor -> repositories.add(new Repository(descriptor
                , urlWithSlash + descriptor.getKey())));
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(
            List<Repository> repositories) {
        this.repositories = repositories;
    }
}
