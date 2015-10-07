package org.artifactory.ui.rest.service.builds.buildsinfo.tabs;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildGeneralInfoService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildGeneralInfoService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String buildName = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String date = request.getPathParamByKey("date");
            String buildStarted = null;
            if (StringUtils.isNotBlank(date)) {
                buildStarted = DateUtils.formatBuildDate(Long.parseLong(date));
            }
            // get Build and update response
            getBuildAndUpdateResponse(response, buildName, buildNumber, buildStarted);
        } catch (ParseException e) {
            response.error("problem with fetching builds");
        }
    }

    /**
     * populate build info from build model and update response
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param buildName           - build name
     * @param buildNumber         - build number
     * @param buildStarted        - build start date
     */
    private void getBuildAndUpdateResponse(RestResponse artifactoryResponse, String buildName, String buildNumber,
            String buildStarted) {
        Build build = getBuild(buildName, buildNumber, buildStarted, artifactoryResponse);
        Long time = null;
        try {
            time = DateUtils.toBuildDate(build.getStarted());
        } catch (Exception e) {
            log.warn("Fail to parse the build started field: setting it as null.");
        }
        String buildAgent = (build.getBuildAgent() == null) ? null : build.getBuildAgent().toString();
        GeneralBuildInfo generalBuildInfo = new GeneralBuildInfo(new GeneralBuildInfo.BuildBuilder()
                .buildName(build.getName())
                .lastBuildTime(buildStarted)
                .agent(build.getAgent().toString())
                .buildAgent(buildAgent)
                .artifactoryPrincipal(build.getArtifactoryPrincipal())
                .principal(build.getPrincipal())
                .duration(DateUtils.getDuration(build.getDurationMillis()))
                .buildNumber(buildNumber)
                .time(time)
                .url(build.getUrl()));
        artifactoryResponse.iModel(generalBuildInfo);
    }

    /**
     * get build model
     *
     * @param buildName    - build name
     * @param buildNumber  - build number
     * @param buildStarted - build start date
     * @param response     - encapsulate data require for response
     * @return
     */
    private Build getBuild(String buildName, String buildNumber, String buildStarted, RestResponse response) {
        boolean buildStartedSupplied = StringUtils.isNotBlank(buildStarted);
        try {
            Build build = null;
            build = getBuild(buildName, buildNumber, buildStarted, buildStartedSupplied, build);
            if (build == null) {
                StringBuilder builder = new StringBuilder().append("Could not find build '").append(buildName).
                        append("' #").append(buildNumber);
                if (buildStartedSupplied) {
                    builder.append(" that started at ").append(buildStarted);
                }
                response.error(builder.toString());
            }
            return build;
        } catch (RepositoryRuntimeException e) {
            String errorMessage = new StringBuilder().append("Error locating latest build for '").append(buildName).
                    append("' #").append(buildNumber).append(": ").append(e.getMessage()).toString();
            response.error(errorMessage);
        }
        return null;
    }

    /**
     * get build general info
     *
     * @param buildName            - build n name
     * @param buildNumber          - build number
     * @param buildStarted         - build started date
     * @param buildStartedSupplied - build started supplier
     * @param build                - run build model
     * @return - build model
     */
    private Build getBuild(String buildName, String buildNumber, String buildStarted, boolean buildStartedSupplied,
            Build build) {
        if (buildStartedSupplied) {
            BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
            if (buildRun != null) {
                build = buildService.getBuild(buildRun);
            }
        } else {
            //Take the latest build of the specified number
            build = buildService.getLatestBuildByNameAndNumber(buildName, buildNumber);
        }
        return build;
    }
}
