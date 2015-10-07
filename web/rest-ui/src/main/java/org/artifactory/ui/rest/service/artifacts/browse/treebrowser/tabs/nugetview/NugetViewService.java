package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.nugetview;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.nuget.UiNuGetAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.nuget.NuMetaData;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.nugetinfo.NugetArtifactInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NugetViewService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NugetArtifactInfo nugetArtifactInfo = (NugetArtifactInfo) request.getImodel();
        // get nuGet Meta data
        NugetArtifactInfo nuGetMetaData = getNuGetMetaData(nugetArtifactInfo);
        // update response with model data
        if (nuGetMetaData != null) {
            response.iModel(nuGetMetaData);
        }
    }

    /**
     * get  Nuget MetaData model
     *
     * @param nugetArtifactInfo
     * @return nuGet Meta Data model
     */
    private NugetArtifactInfo getNuGetMetaData(NugetArtifactInfo nugetArtifactInfo) {
        String repoKey = nugetArtifactInfo.getRepoKey();
        String path = nugetArtifactInfo.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        UiNuGetAddon uiNuGetAddon = addonsManager.addonByType(UiNuGetAddon.class);
        if (uiNuGetAddon != null) {
            NuMetaData nugetSpecMetaData = uiNuGetAddon.getNutSpecMetaData(repoPath);
            nugetArtifactInfo = new NugetArtifactInfo(nugetSpecMetaData);
        }
        return nugetArtifactInfo;
    }
}
