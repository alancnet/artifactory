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
public class DiffBuildModuleArtifactService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String moduleId = request.getPathParamByKey("id");
        String buildNumber = request.getPathParamByKey("number");
        String comparedBuildNum = request.getQueryParamByKey("otherNumber");
        String comparedDate = request.getQueryParamByKey("otherDate");
        PagingData pagingData = request.getPagingData();
        String buildStarted = request.getPathParamByKey("date");
        BuildParams buildParams = new BuildParams(moduleId, buildNumber, comparedBuildNum, comparedDate, buildStarted, name);
        List<ModuleArtifact> moduleArtifacts = buildService.getModuleArtifactsForDiffWithPaging(buildParams,
                pagingData.getStartOffset(), pagingData.getLimit());
        List<ModuleArtifactModel> moduleArtifactModels = new ArrayList<>();
//        int count = 0;
        if (moduleArtifacts != null && !moduleArtifacts.isEmpty()) {
            /*
            count = buildService.getModuleArtifactsForDiffCount(buildParams,
                    pagingData.getStartOffset(), pagingData.getLimit());
            */
            moduleArtifacts.forEach(moduleArtifact -> {
                ModuleArtifactModel artifactModel = new ModuleArtifactModel(moduleArtifact, ActionUtils.getDownloadLink(request.getServletRequest(),
                        moduleArtifact.getRepoKey(), moduleArtifact.getPath()));
                moduleArtifactModels.add(artifactModel);
            });
        }
        PagingModel pagingModel = new PagingModel(0, moduleArtifactModels);
        response.iModel(pagingModel);
    }
}
