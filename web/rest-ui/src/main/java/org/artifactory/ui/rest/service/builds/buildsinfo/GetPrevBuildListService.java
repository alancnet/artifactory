package org.artifactory.ui.rest.service.builds.buildsinfo;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPrevBuildListService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetPrevBuildListService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        String buildName = request.getPathParamByKey("name");
        String date = request.getPathParamByKey("date");
        // fetch build info data
        fetchAllBuildsData(response, buildName, date);
    }

    /**
     * fetch all build data by type
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param pagingData          - paging data sa send from client
     */
    /**
     * fetch all build data by type
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param buildName           - current build name
     * @param buildDate           - current build date
     */
    private void fetchAllBuildsData(RestResponse artifactoryResponse, String buildName, String buildDate) {
        List<GeneralBuild> prevBuildsList = buildService.getPrevBuildsList(buildName, buildDate);
        List<GeneralBuildInfo> generalBuildInfoList = new ArrayList<>();
        prevBuildsList.forEach(buildRun ->
                generalBuildInfoList.add(new GeneralBuildInfo(new GeneralBuildInfo.BuildBuilder().buildNumber(buildRun.getBuildNumber()).
                        time(buildRun.getBuildDate()))));
        artifactoryResponse.iModelList(generalBuildInfoList);
    }
}
