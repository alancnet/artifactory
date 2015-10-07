package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.repo.RepositoryService;
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
public class ZapArtifactService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ZapArtifact zapArtifact = (ZapArtifact) request.getImodel();
        String repoKey = zapArtifact.getRepoKey();
        String path = zapArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        // zap caches
        ZapArtifact(repoPath);
        response.info("Completed zapping item: '" + repoPath + "'");
    }

    /**
     * zap artifact caches
     *
     * @param repoPath - repository path
     */
    private void ZapArtifact(RepoPath repoPath) {
        repositoryService.zap(repoPath);
    }
}
