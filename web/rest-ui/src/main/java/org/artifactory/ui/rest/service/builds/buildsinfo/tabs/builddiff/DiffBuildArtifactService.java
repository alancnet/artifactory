package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.PagingData;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.ModuleArtifactModel;
import org.artifactory.ui.utils.ActionUtils;
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
public class DiffBuildArtifactService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String moduleId = request.getPathParamByKey("id");
        String buildNumber = request.getPathParamByKey("number");
        String comparedBuildNum = request.getQueryParamByKey("otherNumber");
        String comparedDate = request.getQueryParamByKey("otherDate");
        String buildStarted = request.getPathParamByKey("date");
        PagingData pagingData = request.getPagingData();
        // fetch build artifact diff data with another build
        fetchBuildArtifactDiffData(request, response, name, moduleId, buildNumber, comparedBuildNum, comparedDate, buildStarted, pagingData);
    }

    /**
     * fetch build artifact diff with another build data
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data require for responsse
     * @param name                - build name
     * @param buildNumber         - current build number
     * @param comparedBuildNum    - compared build number
     * @param comparedDate        - compared build date
     * @param buildStarted        - current build date
     * @param pagingData          - paging data
     */
    private void fetchBuildArtifactDiffData(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse, String name, String moduleId, String buildNumber, String comparedBuildNum, String comparedDate, String buildStarted, PagingData pagingData) {
        BuildParams buildParams = new BuildParams(moduleId, buildNumber, comparedBuildNum, comparedDate, buildStarted, name);
        buildParams.setAllArtifact(true);
        List<ModuleArtifact> moduleArtifacts = buildService.getModuleArtifactsForDiffWithPaging(buildParams,
                pagingData.getStartOffset(), pagingData.getLimit());
        List<ModuleArtifactModel> moduleArtifactModels = new ArrayList<>();
        if (moduleArtifacts != null && !moduleArtifacts.isEmpty()) {
           /* int count = buildService.getModuleArtifactsForDiffCount(buildParams,
                    pagingData.getStartOffset(), pagingData.getLimit());*/
            moduleArtifacts.forEach(moduleArtifact -> {
                String downloadLink = moduleArtifact.getRepoKey() == null ? null : ActionUtils.getDownloadLink(artifactoryRequest.getServletRequest(),
                        moduleArtifact.getRepoKey(), moduleArtifact.getPath());
                ModuleArtifactModel artifactModel = new ModuleArtifactModel(moduleArtifact,
                        downloadLink);
                moduleArtifactModels.add(artifactModel);
            });
            PagingModel pagingModel = new PagingModel(0, moduleArtifactModels);
            artifactoryResponse.iModel(pagingModel);
        }
    }
}
