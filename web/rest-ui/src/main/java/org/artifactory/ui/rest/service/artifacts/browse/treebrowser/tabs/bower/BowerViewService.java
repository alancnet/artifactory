package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.bower;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.bower.BowerAddon;
import org.artifactory.addon.bower.BowerMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.bower.BowerArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BowerViewService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BowerArtifactInfo bowerArtifactInfo = (BowerArtifactInfo) request.getImodel();
        String path = bowerArtifactInfo.getPath();
        String repoKey = bowerArtifactInfo.getRepoKey();
        // get bower meta data model
        BowerArtifactInfo bowerArtifactMetadata = getBowerArtifactInfoModel(path, repoKey);
        // update response with model
        response.iModel(bowerArtifactMetadata);
    }

    /**
     * get bower artifact info metadata model
     *
     * @param path    - ppath
     * @param repoKey - repo key
     * @return bower meta data artifact info
     */
    private BowerArtifactInfo getBowerArtifactInfoModel(String path, String repoKey) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        BowerAddon bowerAddon = addonsManager.addonByType(BowerAddon.class);
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        FileInfo fileInfo = repositoryService.getFileInfo(repoPath);
        BowerMetadataInfo bowerMetadata = bowerAddon.getBowerMetadata(fileInfo);
        if (bowerMetadata != null) {
            return new BowerArtifactInfo(bowerMetadata);
        }
        return null;
    }
}
