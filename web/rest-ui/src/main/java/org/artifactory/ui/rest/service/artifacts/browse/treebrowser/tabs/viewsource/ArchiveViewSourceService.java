package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.viewsource;

import org.artifactory.api.repo.ArchiveFileContent;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.viewsource.ViewArtifactSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArchiveViewSourceService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ArchiveViewSourceService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ViewArtifactSource viewArtifact = (ViewArtifactSource) request.getImodel();
        String archivePath = viewArtifact.getArchivePath();
        String repoKey = viewArtifact.getRepoKey();
        String sourcePath = viewArtifact.getSourcePath();
        RepoPath archiveRepoPath = InternalRepoPathFactory.create(repoKey, archivePath);
        // get source content
        ArchiveFileContent archiveFileContent = getArchiveFileContent(sourcePath, archiveRepoPath);
        if (sourceFileNotFound(archiveFileContent)) {
            archiveFileContent.setContent("Source File not found");
        }
        // update response with model data
        updateResponseWithModelData(response, archiveFileContent);
    }

    /**
     * check if source file not found
     *
     * @param archiveFileContent
     * @return - if true source file not found
     */
    private boolean sourceFileNotFound(ArchiveFileContent archiveFileContent) {
        return archiveFileContent.getContent() == null || archiveFileContent.getContent().length() == 0;
    }

    /**
     * update respponse with model data
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param archiveFileContent  - archive source file content
     */
    private void updateResponseWithModelData(RestResponse artifactoryResponse, ArchiveFileContent archiveFileContent) {
        ViewArtifactSource viewArtifactSource = new ViewArtifactSource();
        viewArtifactSource.setSource(archiveFileContent.getContent());
        artifactoryResponse.iModel(viewArtifactSource);
    }

    /**
     * fetch archive source content
     *
     * @param sourcePath      - source relative path
     * @param archiveRepoPath - archive path
     * @return archive File content
     */
    private ArchiveFileContent getArchiveFileContent(String sourcePath, RepoPath archiveRepoPath) {
        ArchiveFileContent archiveFileContent = null;
        try {
            archiveFileContent = repositoryService.getGenericArchiveFileContent(archiveRepoPath, sourcePath);
        } catch (IOException e) {
            log.error(e.toString());
        }
        return archiveFileContent;
    }
}
