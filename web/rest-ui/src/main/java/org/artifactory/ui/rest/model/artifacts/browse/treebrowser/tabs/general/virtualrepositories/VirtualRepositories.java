package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.virtualrepositories;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.util.PathUtils;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class VirtualRepositories {

    private List<VirtualRepository> virtualRepositories = Lists.newArrayList();

    public VirtualRepositories() {
        // For Jackson
    }

    public VirtualRepositories(List<VirtualRepoDescriptor> virtualRepoList, String baseURL) {
        String urlWithSlash = PathUtils.addTrailingSlash(baseURL);
        virtualRepoList.forEach(descriptor ->
                this.virtualRepositories.add(new VirtualRepository(descriptor.getKey(),
                        urlWithSlash + descriptor.getKey())));
    }

    public List<VirtualRepository> getVirtualRepositories() {
        return virtualRepositories;
    }

    public void setVirtualRepositories(
            List<VirtualRepository> virtualRepositories) {
        this.virtualRepositories = virtualRepositories;
    }
}
