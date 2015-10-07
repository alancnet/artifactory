package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aviad Shikloshi
 */
public class RepositoryInfoListFactory {

    public static List<RepositoryInfo> createRepositoryInfo(String repoType, CentralConfigService configService,
                                                            RepositoryService repositoryService) {
        List<RepositoryInfo> repoInfo;
        switch (repoType) {
            case "local":
                List<LocalRepoDescriptor> localRepoDescriptorList = repositoryService.getLocalRepoDescriptors();
                repoInfo = localRepoDescriptorList.stream()
                        .map(repoDesc -> new LocalRepositoryInfo(repoDesc, configService)).collect(Collectors.toList());
                break;
            case "remote":
                List<RemoteRepoDescriptor> remoteRepoDescriptorList = repositoryService.getRemoteRepoDescriptors();
                repoInfo = remoteRepoDescriptorList.stream()
                        .map(repoDesc -> new RemoteRepositoryInfo(repoDesc, configService.getDescriptor())).collect(Collectors.toList());
                break;
            case "virtual":
                List<VirtualRepoDescriptor> virtualRepoDescriptorList = repositoryService.getVirtualRepoDescriptors();
                repoInfo = virtualRepoDescriptorList.stream()
                        .filter(virtualDescriptor -> !virtualDescriptor.getKey()
                                .equals(VirtualRepoDescriptor.GLOBAL_VIRTUAL_REPO_KEY))
                        .map(VirtualRepositoryInfo::new).collect(Collectors.toList());
                break;
            default:
                repoInfo = Lists.newArrayList();
        }
        return repoInfo;
    }

    private RepositoryInfoListFactory() {
    }

}
