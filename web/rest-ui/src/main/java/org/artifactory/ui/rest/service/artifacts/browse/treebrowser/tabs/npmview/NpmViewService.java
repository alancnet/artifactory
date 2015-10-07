package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.npmview;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.npm.NpmAddon;
import org.artifactory.addon.npm.NpmMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.npm.NpmArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NpmViewService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NpmArtifactInfo npmArtifactInfo = (NpmArtifactInfo) request.getImodel();
        // fetch npm meta data
        fetchNpmMetaData(request, response, npmArtifactInfo);
    }

    /**
     * fetch npm meta data
     *
     * @param artifactoryRequest  - encapsulate data relate to request
     * @param artifactoryResponse - encapsulate data require for response
     * @param npmArtifactInfo     - npm artifact info
     */
    private void fetchNpmMetaData(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse,
            NpmArtifactInfo npmArtifactInfo) {
        String repoKey = npmArtifactInfo.getRepoKey();
        String path = npmArtifactInfo.getPath();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        NpmArtifactInfo npmArtifactInfoModel = (NpmArtifactInfo) artifactoryRequest.getImodel();
        /// get npm add on
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        NpmAddon npmAddon = addonsManager.addonByType(NpmAddon.class);
        if (npmAddon != null) {
            // get npm meta data
            ItemInfo itemInfo = repositoryService.getItemInfo(repoPath);
            NpmMetadataInfo npmMetaDataInfo = npmAddon.getNpmMetaDataInfo((FileInfo) itemInfo);
            npmArtifactInfo.setNpmDependencies(npmMetaDataInfo.getNpmDependencies());
            npmArtifactInfo.setNpmInfo(npmMetaDataInfo.getNpmInfo());
            npmArtifactInfo.clearRepoData();
            artifactoryResponse.iModel(npmArtifactInfoModel);
        }
    }
}
