package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.publishedmodules;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.PublishedModule;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.PagingData;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.BuildModule;
import org.artifactory.ui.utils.ModelDbMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPublishedModulesService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = request.getPathParamByKey("date");
        // fetch modules
        fetchModuleList(request, response, buildNumber, buildStarted);
         }

    /**
     * get
     * @param artifactoryRequest - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for response
     * @param buildNumber - build number
     * @param buildStarted - build started
     */
    private void fetchModuleList(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse, String buildNumber, String buildStarted) {
        PagingData pagingData = artifactoryRequest.getPagingData();
        List<BuildModule> buildModuleList = new ArrayList<>();
        Map<String, String> moduleMap = ModelDbMap.getModuleMap();
        List<PublishedModule> buildModules = buildService.getPublishedModules(buildNumber, buildStarted,
                moduleMap.get(pagingData.getOrderBy()),
                pagingData.getDirection(), pagingData.getStartOffset(), pagingData.getLimit());
        if (buildModules != null) {
            buildModules.forEach(module -> buildModuleList.add(new BuildModule(module)));
            PagingModel pagingModel = new PagingModel(0, buildModuleList);
            artifactoryResponse.iModel(pagingModel);
        }
    }
}
