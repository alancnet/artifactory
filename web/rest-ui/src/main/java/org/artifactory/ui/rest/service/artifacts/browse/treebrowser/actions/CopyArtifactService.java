package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.CopyArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CopyArtifactService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CopyArtifactService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CopyArtifact copyArtifact = (CopyArtifact) request.getImodel();
        // copy artifact to another repo
        copyArtifact(response, copyArtifact);
    }

    /**
     * copy artifact from one repo to another
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param copyArtifact        - copy action model
     */
    private void copyArtifact(RestResponse artifactoryResponse, CopyArtifact copyArtifact) {
        String msg = String.format("Failed to %s from src=%s to target=%s: ", "copy", copyArtifact.getPath(),
                copyArtifact.getTargetPath());
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
            // copy artifact
            MoveMultiStatusHolder copyStatus = copy(copyArtifact);
            // update response data
            updateResponseData(artifactoryResponse, copyArtifact, msg, copyStatus);

    }

    /**
     * update response feedback and http status code
     *
     * @param artifactoryResponse - encapsulate data require for reposne
     * @param copyArtifact        - copy action model
     * @param msg                 - feedback msg
     * @param copyStatus          - copy status holder
     */
    private void updateResponseData(RestResponse artifactoryResponse, CopyArtifact copyArtifact, String msg,
            MoveMultiStatusHolder copyStatus) {
        try {
            if (copyStatus.hasErrors()) {
                artifactoryResponse.responseCode(HttpServletResponse.SC_CONFLICT);
                List<String> errors = new ArrayList<>();
                copyStatus.getErrors().forEach(error -> errors.add(error.getMessage()));
                artifactoryResponse.errors(errors);
            } else {
                artifactoryResponse.info(
                        "Artifacts successfully copied to:" + copyArtifact.getTargetRepoKey() + ":" + copyArtifact.getTargetPath());
            }
        } catch (Exception e) {
            artifactoryResponse.responseCode(HttpServletResponse.SC_CONFLICT);
            List<String> errors = new ArrayList<>();
            copyStatus.getErrors().forEach(error -> errors.add(error.getMessage()));
            artifactoryResponse.errors(errors);
        }
    }

    /**
     * copy artifact from one repo path to another repo path
     *
     * @param copyArtifact - copy action model
     * @return - copy multi status holder
     * @throws Exception
     */
    private MoveMultiStatusHolder copy(CopyArtifact copyArtifact) {

        MoveMultiStatusHolder status;
        RepoPath repoPath = InternalRepoPathFactory.create(copyArtifact.getRepoKey(), copyArtifact.getPath());
        RepoPath targetRepoPath = InternalRepoPathFactory.create(copyArtifact.getTargetRepoKey(),
                copyArtifact.getTargetPath());
        // Force suppressing layouts, we want to be able to copy stuff between layouts
        copyArtifact.setSuppressLayouts(true);
        status = repositoryService.copy(repoPath, targetRepoPath, copyArtifact.isDryRun(),
                copyArtifact.isSuppressLayouts(), copyArtifact.isFailFast());
        if (!status.isError() && !status.hasWarnings()) {
            String opType = (copyArtifact.isDryRun()) ? "Dry run for " : "";
            status.status(
                    String.format("%s copying %s to %s completed successfully, %s artifacts and %s folders were " +
                                    "copied", opType, repoPath, targetRepoPath, status.getMovedArtifactsCount(),
                            status.getMovedFoldersCount()), log);
        }
        return status;
    }
}
