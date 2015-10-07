package org.artifactory.ui.rest.service.builds.buildsinfo;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.BuildCoordinate;
import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.ui.utils.DateUtils;
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
public class DeleteBuildService<T extends DeleteBuildsModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(DeleteBuildService.class);

    @Autowired
    private BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        List<BuildCoordinate> buildsCoordinates = model.getBuildsCoordinates();
        // Delete all coordinates
        for (BuildCoordinate coordinate : buildsCoordinates) {
            // Delete specific build and update response feedback
            deleteSpecificBuildsAndUpdateResponse(response, coordinate);
        }
        if (model.getBuildsCoordinates().size() > 1) {
            response.info("Successfully removed " + model.getBuildsCoordinates().size() + " builds");
        } else if (model.getBuildsCoordinates().size() == 1) {
            BuildCoordinate coordinate = model.getBuildsCoordinates().get(0);
            response.info(
                    "Successfully removed " + coordinate.getBuildName() + " #" + coordinate.getBuildNumber() + " build");
        }
    }

    /**
     * delete Build by "build coordinate"
     */
    private void deleteSpecificBuildsAndUpdateResponse(RestResponse response, BuildCoordinate coordinate) {
        String buildName = coordinate.getBuildName();
        String buildNumber = coordinate.getBuildNumber();
        long buildDate = coordinate.getDate();
        BasicStatusHolder multiStatusHolder = new BasicStatusHolder();
        try {
            String buildStarted = DateUtils.formatBuildDate(buildDate);
            BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
            buildService.deleteBuild(buildRun, false, multiStatusHolder);
            multiStatusHolder.status(String.format("Successfully deleted build '%s' #%s.", buildName, buildNumber),
                    log);
            if (multiStatusHolder.hasErrors()) {
                response.error(multiStatusHolder.getLastError().getMessage());
            } else if (multiStatusHolder.hasWarnings()) {
                List<StatusEntry> warnings = multiStatusHolder.getWarnings();
                response.warn(warnings.get(warnings.size() - 1).getMessage());
                return;
            }
        } catch (Exception exception) {
            String error = String.format("Exception occurred while deleting build '%s' #%s", buildName, buildNumber);
            multiStatusHolder.error(error, exception, log);
            response.error(error);
        }
    }
}
