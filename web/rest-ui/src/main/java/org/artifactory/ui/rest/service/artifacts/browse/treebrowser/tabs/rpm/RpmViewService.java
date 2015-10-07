package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.rpm;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.yum.ArtifactRpmMetadata;
import org.artifactory.addon.yum.YumAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.rpm.RpmArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RpmViewService implements RestService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RpmArtifactInfo rpmArtifactInfo = (RpmArtifactInfo) request.getImodel();
        // get rpm meta data
        ArtifactRpmMetadata rpmMetadata = getArtifactRpmMetadata(rpmArtifactInfo);
        if (rpmMetadata == null) {
            return;
        }
        RpmArtifactInfo rpmReturnMeta = new RpmArtifactInfo(rpmMetadata);
        // update response
        response.iModel(rpmReturnMeta);
    }

    /**
     * get Rpm meta data
     *
     * @param rpmArtifactInfo - rpm meta data from file
     * @return
     */
    private ArtifactRpmMetadata getArtifactRpmMetadata(RpmArtifactInfo rpmArtifactInfo) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        YumAddon yumAddon = addonsManager.addonByType(YumAddon.class);
        RepoPath repoPath = InternalRepoPathFactory.create(rpmArtifactInfo.getRepoKey(), rpmArtifactInfo.getPath());
        FileInfo itemInfo = (FileInfo) repositoryService.getItemInfo(repoPath);
        return yumAddon.getRpmMetadata(itemInfo);
    }
}
