package org.artifactory.ui.rest.service.admin.services.indexer;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.index.MavenIndexerService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class RunIndexNowService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RunIndexNowService.class);

    @Autowired
    private MavenIndexerService mavenIndexer;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("RunIndexNow");
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        try {
            // run index now
            runIndexNow(response);
        } catch (Exception e) {
            updateErrorFeedBack(response, statusHolder, e);
        }
    }

    /**
     * update error feedback
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param statusHolder        - status holder
     * @param e                   - exception
     */
    private void updateErrorFeedBack(RestResponse artifactoryResponse, BasicStatusHolder statusHolder, Exception e) {
        log.error("Could not run indexer.", e);
        statusHolder.error(e.getMessage(), log);
        artifactoryResponse.error("Indexer did not run: " + e.getMessage());
    }

    /**
     * schedule indexing for now
     *
     * @param artifactoryResponse - encapsulate data require for response
     */
    private void runIndexNow(RestResponse artifactoryResponse) {
        BasicStatusHolder status = new BasicStatusHolder();
        mavenIndexer.scheduleImmediateIndexing(status);
        if (status.isError()) {
            artifactoryResponse.error(status.getStatusMsg());
        } else {
            artifactoryResponse.info("Indexer was successfully scheduled to run in the background.");
        }
    }
}
