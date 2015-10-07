package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ZapArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ZapCachesVirtualService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ZapArtifact zapArtifact = (ZapArtifact) request.getImodel();
        String repoKey = zapArtifact.getRepoKey();
        String path = zapArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        // un-deploy virtual
        StatusHolder statusHolder = repositoryService.undeploy(repoPath, false);
        // update response status
        updateResponseStatus(response, repoKey, statusHolder);
    }

    /**
     * update response with zap caches results
     *
     * @param response     - encapsulate data related to response
     * @param repoKey      - repo key
     * @param statusHolder - zap caches action status holder
     */
    private void updateResponseStatus(RestResponse response, String repoKey, StatusHolder statusHolder) {
        if (!statusHolder.isError()) {
            response.info("The caches of '" + repoKey + "' have been successfully zapped.");
        } else {
            String message = "Could not zap caches for the virtual repository '" + repoKey + "': " +
                    statusHolder.getStatusMsg() + "";
            response.error(message);
        }
    }
}
