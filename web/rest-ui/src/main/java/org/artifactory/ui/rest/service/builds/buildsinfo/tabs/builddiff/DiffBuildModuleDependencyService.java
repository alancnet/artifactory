package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.PagingData;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.ModuleDependencyModel;
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
public class DiffBuildModuleDependencyService implements RestService {

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
        List<ModuleDependency> moduleArtifacts = buildService.getModuleDependencyForDiffWithPaging(buildParams,
                pagingData.getStartOffset(), pagingData.getLimit());
        List<ModuleDependencyModel> moduleArtifactModels = new ArrayList<>();
        if (moduleArtifacts != null && !moduleArtifacts.isEmpty()) {
            // int count = buildService.getModuleDependencyForDiffCount(buildParams,
            //     pagingData.getStartOffset(), pagingData.getLimit());
            moduleArtifacts.forEach(moduleArtifact -> {
                ModuleDependencyModel depModel = new ModuleDependencyModel(moduleArtifact, ActionUtils.getDownloadLink(request.getServletRequest(),
                        moduleArtifact.getRepoKey(), moduleArtifact.getPath()));
                moduleArtifactModels.add(depModel);
            });
            PagingModel pagingModel = new PagingModel(0, moduleArtifactModels);
            response.iModel(pagingModel);
        }
    }
}
