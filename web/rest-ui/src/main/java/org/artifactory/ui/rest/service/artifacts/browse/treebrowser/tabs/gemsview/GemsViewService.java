package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.gemsview;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.gems.ArtifactGemsInfo;
import org.artifactory.addon.gems.GemsAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsDependency;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems.GemsInfo;
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
public class GemsViewService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        GemsArtifactInfo gemsArtifactInfo = (GemsArtifactInfo) request.getImodel();
        // fetch gems info
        fetchGemsInfo(gemsArtifactInfo);
        //update artifactory response with model data
        response.iModel(gemsArtifactInfo);
    }

    /**
     * fetch gems info meta data
     *
     * @param gemsArtifactInfo
     */
    private void fetchGemsInfo(GemsArtifactInfo gemsArtifactInfo) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        GemsAddon gemsAddon = addonsManager.addonByType(GemsAddon.class);
        if (gemsAddon != null) {
            String repoKey = gemsArtifactInfo.getRepoKey();
            String path = gemsArtifactInfo.getPath();
            ArtifactGemsInfo gemsInfo = gemsAddon.getGemsInfo(repoKey, path);
            GemsInfo artifactGemsInfo = new GemsInfo(gemsInfo, repoKey, path);
            gemsArtifactInfo.clearRepoData();
            gemsArtifactInfo.setGemsInfo(artifactGemsInfo);
            List<GemsDependency> gemsDependencies = new ArrayList<>();
            gemsInfo.getDependencies().getDevelopment().forEach(dev ->
                    gemsDependencies.add(new GemsDependency(dev.getName(), dev.getRequirements(), "Development")));
            gemsInfo.getDependencies().getRuntime().forEach(runtime ->
                    gemsDependencies.add(new GemsDependency(runtime.getName(), runtime.getRequirements(), "Runtime")));
            gemsArtifactInfo.setGemsDependencies(gemsDependencies);
        }
    }
}
