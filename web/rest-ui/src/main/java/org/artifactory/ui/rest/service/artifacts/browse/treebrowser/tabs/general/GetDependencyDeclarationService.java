package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.dependecydeclaration.DependencyDeclaration;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class GetDependencyDeclarationService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DependencyDeclaration dependencyDeclaration = new DependencyDeclaration();
        //update DependencyDeclaration model data
        updateDependencyDeclarationModel(request, dependencyDeclaration);
        // update response
        response.iModel(dependencyDeclaration);
    }

    /**
     * update DependencyDeclaration model data
     * @param artifactoryRequest - encapsulate data related to request
     * @param dependencyDeclaration - dependencyDeclaration model
     */
    private void updateDependencyDeclarationModel(ArtifactoryRestRequest artifactoryRequest,
            DependencyDeclaration dependencyDeclaration) {
        String repoKey = artifactoryRequest.getQueryParamByKey(RequestUtils.REPO_KEY_PARAM);
        RepoPath targetRepoPath = RequestUtils.getPathFromRequest(artifactoryRequest);
        ItemInfo itemInfo = repositoryService.getItemInfo(targetRepoPath);
        LocalRepoDescriptor localRepoDescriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        dependencyDeclaration.updateDependencyDeclaration(artifactoryRequest, repositoryService,
                                 itemInfo, localRepoDescriptor);
        dependencyDeclaration.setTypes(null);
    }

}
