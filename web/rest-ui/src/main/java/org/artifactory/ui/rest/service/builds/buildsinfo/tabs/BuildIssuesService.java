package org.artifactory.ui.rest.service.builds.buildsinfo.tabs;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.ui.rest.model.builds.IssueModel;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Issue;
import org.jfrog.build.api.Issues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildIssuesService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(BuildIssuesService.class);


    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String buildNumber = request.getPathParamByKey("number");
        try {
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            // fetch build issues
            fetchBuildIssues(response, name, buildNumber, buildStarted);
        } catch (ParseException e) {
            log.error(e.toString());
        }
    }

    /**
     * @param artifactoryResponse - encapsulate data related to response
     * @param name                - build name
     * @param buildNumber         - build number
     * @param buildStarted        - build date
     */
    private void fetchBuildIssues(RestResponse artifactoryResponse, String name, String buildNumber, String buildStarted) {
        Build build = getBuild(name, buildNumber, buildStarted, artifactoryResponse);
        Issues issues = build.getIssues();
        if (issues == null) {
            artifactoryResponse.iModelList(new ArrayList<>());
            return;
        }
        Set<Issue> affectedIssues = issues.getAffectedIssues();
        List<IssueModel> issuesList = new ArrayList<>();
        affectedIssues.forEach(issue -> issuesList.add(new IssueModel(issue)));
        artifactoryResponse.iModelList(issuesList);
    }

    /**
     * get build info
     *
     * @param buildName    - build name
     * @param buildNumber  - build number
     * @param buildStarted - build date
     * @param response     - encapsulate data related to request
     * @return
     */
    private Build getBuild(String buildName, String buildNumber, String buildStarted, RestResponse response) {
        boolean buildStartedSupplied = StringUtils.isNotBlank(buildStarted);
        try {
            Build build = null;
            if (buildStartedSupplied) {
                BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
                if (buildRun != null) {
                    build = buildService.getBuild(buildRun);
                }
            } else {
                //Take the latest build of the specified number
                build = buildService.getLatestBuildByNameAndNumber(buildName, buildNumber);
            }
            if (build == null) {
                StringBuilder builder = new StringBuilder().append("Could not find build '").append(buildName).
                        append("' #").append(buildNumber);
                if (buildStartedSupplied) {
                    builder.append(" that started at ").append(buildStarted);
                }
                throwNotFoundError(response, builder.toString());
            }
            return build;
        } catch (RepositoryRuntimeException e) {
            String errorMessage = new StringBuilder().append("Error locating latest build for '").append(buildName).
                    append("' #").append(buildNumber).append(": ").append(e.getMessage()).toString();
            throwInternalError(errorMessage, response);
        }
        //Should not happen
        return null;
    }

    /**
     * Throws a 404 AbortWithHttpErrorCodeException with the given message
     *
     * @param errorMessage Message to display in the error
     */
    private void throwNotFoundError(RestResponse response, String errorMessage) {
        log.error(errorMessage);
        response.error(errorMessage);
    }

    /**
     * return not found error
     *
     * @param errorMessage
     * @param response
     */
    private void throwInternalError(String errorMessage, RestResponse response) {
        response.error(errorMessage);
        response.responseCode(HttpServletResponse.SC_NOT_FOUND);
    }

}
