package org.artifactory.ui.rest.service.builds.buildsinfo;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.BuildCoordinate;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import java.util.List;

/**
 * @author Chen Keinans
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
public class DeleteAllBuildsService<T extends DeleteBuildsModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(DeleteAllBuildsService.class);

    @Autowired
    private BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        for (BuildCoordinate coordinate : model.getBuildsCoordinates()) {
            // delete all builds and update response
            deleteAllBuildsAndUpdateResponse(response, coordinate.getBuildName());
        }
        if (model.getBuildsCoordinates().size() > 1) {
            response.info("Successfully removed " + model.getBuildsCoordinates().size() + " build projects");
        } else if (model.getBuildsCoordinates().size() == 1) {
            response.info(
                    "Successfully removed " + model.getBuildsCoordinates().get(0).getBuildName() + " build project");
        }
    }

    /**
     * delete all builds and update response
     *
     * @param artifactoryResponse - delete all build and update response
     */
    private void deleteAllBuildsAndUpdateResponse(RestResponse artifactoryResponse, String buildName) {
        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        try {
            // delete all builds by name
            buildService.deleteAllBuilds(buildName);
            multiStatusHolder.status(String.format("Successfully deleted all " + buildName + " builds"),
                    log);
            if (multiStatusHolder.hasErrors()) {
                artifactoryResponse.error(multiStatusHolder.getLastError().getMessage());
            } else if (multiStatusHolder.hasWarnings()) {
                List<StatusEntry> warnings = multiStatusHolder.getWarnings();
                artifactoryResponse.warn(warnings.get(warnings.size() - 1).getMessage());
                return;
            }
        } catch (Exception exception) {
            String error = String.format("Exception occurred while deleting all builds");
            multiStatusHolder.error(error, exception, log);
        }
    }
}
