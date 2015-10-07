package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DeleteArtifact;
import org.artifactory.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteArtifactService implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DeleteArtifact deleteArtifact = (DeleteArtifact) request.getImodel();
        String repoKey = deleteArtifact.getRepoKey();
        String path = deleteArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        // delete artifact from repo path
        StatusHolder statusHolder = deleteArtifact(repoPath);
        // update response data
        updateResponseData(response, statusHolder);
    }

    /**
     * update response feedback
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param statusHolder        - hold artifact delete status
     */
    private void updateResponseData(RestResponse artifactoryResponse, StatusHolder statusHolder) {
        if (statusHolder.isError()) {
            artifactoryResponse.error(statusHolder.getLastError().getMessage());
        } else {
            artifactoryResponse.info("Successfully deleted artifacts");
        }
    }

    /**
     * un deploy artifact from repo path
     *
     * @param repoPath - artifact repo path
     * @return - status of the un deploy
     */
    protected StatusHolder deleteArtifact(RepoPath repoPath) {
        return repositoryService.undeploy(repoPath, true, true);
    }
}
