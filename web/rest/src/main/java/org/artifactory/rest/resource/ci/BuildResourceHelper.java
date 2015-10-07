package org.artifactory.rest.resource.ci;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.build.BuildRun;
import org.artifactory.util.DoesNotExistException;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public class BuildResourceHelper {

    /**
     * Validates the parameters of the move\copy request and returns the basic build info object if found
     *
     * @param buildName   Name of build to target
     * @param buildNumber Number of build to target
     * @param started     Start date of build to target (can be null)
     * @return Basic info of build to target
     */
    public static BuildRun validateParamsAndGetBuildInfo(String buildName, String buildNumber, String started)
            throws ParseException {

        if (StringUtils.isBlank(buildName)) {
            throw new IllegalArgumentException("Build name cannot be blank.");
        }
        if (StringUtils.isBlank(buildNumber)) {
            throw new IllegalArgumentException("Build number cannot be blank.");
        }

        BuildRun toReturn = getRequestedBuildInfo(buildName, buildNumber, started);

        if (toReturn == null) {
            throw new DoesNotExistException("Cannot find build by the name '" + buildName + "' and the number '" +
                    buildNumber + "' which started on " + started + ".");
        }

        return toReturn;
    }

    /**
     * Returns the basic info object of the build to target
     *
     * @param buildName   Name of build to target
     * @param buildNumber Number of build to target
     * @param started     Start date of build to target (can be null)
     * @return Basic info of build to target
     */
    public static BuildRun getRequestedBuildInfo(String buildName, String buildNumber, String started) {
        BuildService buildService = ContextHelper.get().beanForType(BuildService.class);
        Set<BuildRun> buildRunSet = buildService.searchBuildsByNameAndNumber(buildName, buildNumber);
        if (buildRunSet.isEmpty()) {
            throw new DoesNotExistException("Cannot find builds by the name '" + buildName + "' and the number '" +
                    buildNumber + "'.");
        }
        BuildRun toReturn = null;

        if (StringUtils.isBlank(started)) {
            for (BuildRun buildRun : buildRunSet) {
                if ((toReturn == null) || toReturn.getStartedDate().before(buildRun.getStartedDate())) {
                    toReturn = buildRun;
                }
            }
        } else {
            Date requestedStartDate = new Date(BuildInfoUtils.parseBuildTime(started));
            for (BuildRun buildRun : buildRunSet) {
                if (buildRun.getStartedDate().equals(requestedStartDate)) {
                    toReturn = buildRun;
                    break;
                }
            }
        }
        return toReturn;
    }
}
