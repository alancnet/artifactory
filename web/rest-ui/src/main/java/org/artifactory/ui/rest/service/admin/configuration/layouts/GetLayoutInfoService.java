package org.artifactory.ui.rest.service.admin.configuration.layouts;

import com.google.common.collect.Lists;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.layouts.RepositoryAssociationsModel;
import org.artifactory.ui.rest.model.admin.configuration.layouts.RepositoryLayoutModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Lior Hasson
 */
@Component
public class GetLayoutInfoService implements RestService<RepositoryLayoutModel> {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private RepositoryService repositoryService;

    private List<RepoDescriptor> descriptors;

    @Override
    public void execute(ArtifactoryRestRequest<RepositoryLayoutModel> artifactoryRequest,
                        RestResponse artifactoryResponse) {
        List<RepoLayout> repoLayouts = getMutableDescriptor().getRepoLayouts();

        RepositoryLayoutModel layoutModel = getRepositoryLayoutModel(artifactoryRequest, repoLayouts);
        createAssociations(layoutModel);

        artifactoryResponse.iModel(layoutModel);
    }

    private RepositoryLayoutModel getRepositoryLayoutModel(ArtifactoryRestRequest<RepositoryLayoutModel> artifactoryRequest,
                                                           List<RepoLayout> repoLayouts) {
        String layoutKey = artifactoryRequest.getPathParamByKey("layoutKey");
        Optional<RepositoryLayoutModel> layoutModel = repoLayouts.stream()
                .filter(repoLayout -> repoLayout.getName().equals(layoutKey))
                .map(RepositoryLayoutModel::new)
                .findFirst();

        return layoutModel.get();
    }


    private void createAssociations(RepositoryLayoutModel layoutModel){
        descriptors = getAllDescriptors();
            RepositoryAssociationsModel ra = new RepositoryAssociationsModel();

            ra.setLocalRepositories(filterLocalRepos(filterDescriptorsWithLayout(layoutModel)));
            ra.setRemoteRepositories(filterRemoteRepos(filterDescriptorsWithLayout(layoutModel)));
            ra.setVirtualRepositories(filterVirtualRepos(filterDescriptorsWithLayout(layoutModel)));

        layoutModel.setRepositoryAssociations(ra);
    }

    private List<String> filterLocalRepos(List<RepoDescriptor> layoutDescriptors) {
        return layoutDescriptors.stream()
                .filter(desc -> desc instanceof LocalRepoDescriptor)
                .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
    }

    private List<String> filterRemoteRepos(List<RepoDescriptor> layoutDescriptors) {
        return layoutDescriptors.stream()
                .filter(desc -> desc instanceof RemoteRepoDescriptor)
                .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
    }

    private List<String> filterVirtualRepos(List<RepoDescriptor> layoutDescriptors) {
        return layoutDescriptors.stream()
                .filter(desc -> !desc.isReal())
                .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .map(RepoDescriptor::getKey)
                .collect(Collectors.toList());
    }

    private List<RepoDescriptor> filterDescriptorsWithLayout(final RepoLayout layout) {
        return descriptors.stream()
                .filter(desc -> (desc.getRepoLayout() != null && desc.getRepoLayout().equals(layout)))
                .collect(Collectors.toList());
    }

    private List<RepoDescriptor> getAllDescriptors() {
        descriptors = Lists.newArrayList();
        descriptors.addAll(repositoryService.getLocalRepoDescriptors());
        descriptors.addAll(repositoryService.getRemoteRepoDescriptors());
        descriptors.addAll(repositoryService.getVirtualRepoDescriptors());
        return descriptors;
    }

    private MutableCentralConfigDescriptor getMutableDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }
}
