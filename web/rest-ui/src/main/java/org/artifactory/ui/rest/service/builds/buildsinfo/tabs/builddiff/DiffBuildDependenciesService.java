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
public class DiffBuildDependenciesService implements RestService {

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String name = request.getPathParamByKey("name");
        String buildNumber = request.getPathParamByKey("number");
        String comparedBuildNum = request.getQueryParamByKey("otherNumber");
        String comparedDate = request.getQueryParamByKey("otherDate");
        String buildStarted = request.getPathParamByKey("date");
        boolean excludeInternalDependencies = Boolean.valueOf(request.getQueryParamByKey("exDep"));
        PagingData pagingData = request.getPagingData();
        // fetch build artifact diff data
        fetchBuildDependenciesDiffData(request, response, name,
                buildNumber, comparedBuildNum, comparedDate,
                buildStarted, pagingData, excludeInternalDependencies);
    }

    /**
     * fetch build dependencies diff with another build data
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
    private void fetchBuildDependenciesDiffData(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse,
                                                String name, String buildNumber, String comparedBuildNum,
                                                String comparedDate, String buildStarted,
                                                PagingData pagingData, boolean excludeInternalDependencies) {
        // create build query param
        BuildParams buildParams = new BuildParams(null, buildNumber, comparedBuildNum, comparedDate, buildStarted, name);
        buildParams.setAllArtifact(true);
        if (excludeInternalDependencies) {
            buildParams.setExcludeInternalDependencies(true);
        }
        // run build diff query
        List<ModuleDependency> moduleArtifacts = buildService.getModuleDependencyForDiffWithPaging(buildParams,
                pagingData.getStartOffset(), pagingData.getLimit());
        List<ModuleDependencyModel> moduleArtifactModels = new ArrayList<>();
        int count = 0;
        if (moduleArtifacts != null && !moduleArtifacts.isEmpty()) {
           /* count = buildService.getModuleDependencyForDiffCount(buildParams,
                    pagingData.getStartOffset(), pagingData.getLimit());*/
            // populate query response to model
            moduleArtifacts.forEach(moduleArtifact -> {
                String downloadLink = moduleArtifact.getRepoKey() == null ? null : ActionUtils.getDownloadLink(artifactoryRequest.getServletRequest(),
                        moduleArtifact.getRepoKey(), moduleArtifact.getPath());
                ModuleDependencyModel artifactModel = new ModuleDependencyModel(moduleArtifact, downloadLink);
                moduleArtifactModels.add(artifactModel);
            });
            // update paging model
        }
        PagingModel pagingModel = new PagingModel(count, moduleArtifactModels);
        artifactoryResponse.iModel(pagingModel);
    }
}
