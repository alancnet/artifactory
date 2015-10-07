package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff;

import org.artifactory.api.build.BuildProps;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.PagingData;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.BuildPropsModel;
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
public class DiffBuildPropsService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String buildNumber = request.getPathParamByKey("number");
        String comparedBuildNum = request.getQueryParamByKey("otherNumber");
        String comparedDate = request.getQueryParamByKey("otherDate");
        String buildStarted = request.getPathParamByKey("date");
        PagingData pagingData = request.getPagingData();
        // fetch build artifact diff data with another build
        fetchBuildArtifactDiffData(response, name, buildNumber,
                comparedBuildNum, pagingData, comparedDate, buildStarted);
    }

    /**
     * fetch build props diff with another build data
     *
     * @param artifactoryResponse - encapsulate data require for response
     * @param name                - build name
     * @param buildNumber         - current build number
     * @param comparedBuildNum    - compared build number
     * @param pagingData          - paging data
     */
    private void fetchBuildArtifactDiffData(RestResponse artifactoryResponse, String name, String buildNumber,
                                            String comparedBuildNum, PagingData pagingData, String comparedDate, String currDate) {
        BuildParams buildParams = new BuildParams(null, buildNumber, comparedBuildNum, comparedDate, currDate, name);
        buildParams.setAllArtifact(true);
        List<BuildProps> buildProps = buildService.getBuildProps(buildParams, pagingData.getStartOffset(), pagingData.getLimit());
        List<BuildPropsModel> buildPropsModels = new ArrayList<>();
        int count = 0;
        if (buildProps != null && !buildProps.isEmpty()) {
//            count = buildService.getPropsDiffCount(buildParams);
            buildProps.forEach(propsConsumer -> {
                BuildPropsModel propsModel = new BuildPropsModel(propsConsumer);
                buildPropsModels.add(propsModel);
            });
        }
        PagingModel pagingModel = new PagingModel(count, buildPropsModels);
        artifactoryResponse.iModel(pagingModel);
    }
}
