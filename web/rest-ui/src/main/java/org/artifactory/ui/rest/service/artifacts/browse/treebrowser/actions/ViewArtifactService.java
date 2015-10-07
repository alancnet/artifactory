package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewArtifactService implements RestService {

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ViewArtifact viewArtifact = (ViewArtifact) request.getImodel();
        // get file info
        ItemInfo itemInfo = getItemInfo(viewArtifact);
        /// get file content
        String fileContent = getFileContent((FileInfo) itemInfo);
        // update response with file content
        updateResponse(response, fileContent);
    }

    /**
     * get item to be reviewed info
     *
     * @param viewArtifact - view artifact model
     * @return - item info instance
     */
    private ItemInfo getItemInfo(ViewArtifact viewArtifact) {
        String repoKey = viewArtifact.getRepoKey();
        String path = viewArtifact.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        return repositoryService.getItemInfo(repoPath);
    }

    /**
     * update file content response
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param fileContent         - file content
     */
    private void updateResponse(RestResponse artifactoryResponse, String fileContent) {
        RestModel artifact = new ViewArtifact();
        ((ViewArtifact) artifact).setFileContent(fileContent);
        artifactoryResponse.iModel(artifact);
    }

    /**
     * get file content by file type
     *
     * @param fileInfo - file item to be reviewed
     * @return - file content
     */
    private String getFileContent(org.artifactory.fs.FileInfo fileInfo) {
        return repositoryService.getStringContent(fileInfo);
    }
}
