package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aviad Shikloshi
 */
public class RepositoryFieldsValuesModel {

    private List<String> repositoryLayouts;
    private List<String> packageTypes;
    private List<String> webStartKeyPairs;
    private List<String> availableLocalRepos;
    private List<String> availableRemoteRepos;
    private List<String> availableVirtualRepos;
    private List<String> proxies;
    private String defaultProxy;

    public RepositoryFieldsValuesModel(CentralConfigDescriptor descriptor, RepositoryService repositoryService) {
        repositoryLayouts = descriptor.getRepoLayouts().stream().map(RepoLayout::getName).collect(Collectors.toList());
        packageTypes = Lists.newArrayList(RepoType.values()).stream().map(RepoType::name).collect(Collectors.toList());
        webStartKeyPairs = ContextHelper.get().beanForType(AddonsManager.class).addonByType(ArtifactWebstartAddon.class).getKeyPairNames();
        availableLocalRepos = repositoryService.getLocalRepoDescriptors().stream().map(LocalRepoDescriptor::getKey).collect(Collectors.toList());
        availableRemoteRepos = repositoryService.getRemoteRepoDescriptors().stream().map(RemoteRepoDescriptor::getKey).collect(Collectors.toList());
        availableVirtualRepos = repositoryService.getVirtualRepoDescriptors().stream().map(VirtualRepoDescriptor::getKey).collect(Collectors.toList());
        CentralConfigDescriptor centralDescriptor = ContextHelper.get().beanForType(CentralConfigService.class).getDescriptor();
        proxies = Lists.newArrayList(centralDescriptor.getProxies().parallelStream().map(ProxyDescriptor::getKey).collect(Collectors.toList()));
        ProxyDescriptor proxy = descriptor.getDefaultProxy();
        if (proxy != null) {
            defaultProxy = proxy.getKey();
        }
    }

    public List<String> getRepositoryLayouts() {
        return repositoryLayouts;
    }

    public List<String> getPackageTypes() {
        return packageTypes;
    }

    public List<String> getAvailableLocalRepos() {
        return availableLocalRepos;
    }

    public List<String> getAvailableRemoteRepos() {
        return availableRemoteRepos;
    }

    public List<String> getAvailableVirtualRepos() {
        return availableVirtualRepos;
    }

    public List<String> getWebStartKeyPairs() {
        return webStartKeyPairs;
    }

    public List<String> getProxies() {
        return proxies;
    }

    public String getDefaultProxy() {
        return defaultProxy;
    }
}
