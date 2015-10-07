package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.MoveArtifact;
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
public class MoveArtifactService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(MoveArtifactService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MoveArtifact moveArtifact = (MoveArtifact) request.getImodel();
        // move artifact to another repo
        moveArtifact(response, moveArtifact);
    }


    /**
     * copy artifact from one repo to another
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param moveAction          - copy action model
     */
    private void moveArtifact(RestResponse artifactoryResponse, MoveArtifact moveAction) {
        String msg = String.format("Failed to %s from src=%s to target=%s: ", "move", moveAction.getPath(),
                moveAction.getTargetPath());
        MoveMultiStatusHolder status = new MoveMultiStatusHolder();
        try {
            // copy artifact
            MoveMultiStatusHolder copyStatus = move(moveAction);
            // update response data
            updateResponseData(artifactoryResponse, moveAction, msg, copyStatus);
        } catch (Exception e) {
            log.debug("{}", e);
            status.error(msg + e.getMessage(), e, log);
        }
    }

    /**
     * move artifact from one repo path to another repo path
     *
     * @param moveArtifact - copy action model
     * @return - move multi status holder
     * @throws Exception
     */
    private MoveMultiStatusHolder move(MoveArtifact moveArtifact) throws Exception {
        MoveMultiStatusHolder status;
        RepoPath repoPath = InternalRepoPathFactory.create(moveArtifact.getRepoKey(), moveArtifact.getPath());
        RepoPath targetRepoPath = InternalRepoPathFactory.create(moveArtifact.getTargetRepoKey(),
                moveArtifact.getTargetPath());
        // Force suppressing layouts, we want to be able to move stuff between layouts
        moveArtifact.setSuppressLayouts(true);
        status = repositoryService.move(repoPath, targetRepoPath, moveArtifact.isDryRun(),
                moveArtifact.isSuppressLayouts(), moveArtifact.isFailFast());
        if (!status.isError() && !status.hasWarnings()) {
            String opType = (moveArtifact.isDryRun()) ? "Dry run for " : "";
            status.status(String.format("%s moving %s to %s completed successfully, %s artifacts and %s folders were " +
                            "moved", opType, repoPath, targetRepoPath, status.getMovedArtifactsCount(),
                    status.getMovedFoldersCount()), log);
        }
        return status;
    }

    /**
     * update response feedback and http status code
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param moveArtifact        - copy action model
     * @param msg                 - feedback msg
     * @param moveStatus          - copy status holder
     */
    private void updateResponseData(RestResponse artifactoryResponse, MoveArtifact moveArtifact, String msg,
            MoveMultiStatusHolder moveStatus) {
        if (moveStatus.hasErrors()) {
            artifactoryResponse.responseCode(HttpServletResponse.SC_CONFLICT);
            List<String> errors = new ArrayList<>();
            moveStatus.getErrors().forEach(error -> errors.add(error.getMessage()));
            artifactoryResponse.errors(errors);
        } else {
            artifactoryResponse.info(
                    "Artifact successfully moved to:" + moveArtifact.getTargetRepoKey() + ":" + moveArtifact.getTargetPath());
        }
    }
}
