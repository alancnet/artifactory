package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.VirtualBrowsableItem;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.FolderInfo;

/**
 * @author Chen Keinan
 */
public class VirtualRemoteFolderGeneralArtifactInfo extends BaseArtifactInfo {

    private BaseInfo info;

    public VirtualRemoteFolderGeneralArtifactInfo() {
    }

    public VirtualRemoteFolderGeneralArtifactInfo(String name) {
        super(name);
    }

    public void populateGeneralData(BaseBrowsableItem item) {
        BaseInfo baseInfo;
        if (item instanceof VirtualBrowsableItem || item.isRemote()) {
            baseInfo = populateVirtualRemoteFolderInfo(item);
            this.info = baseInfo;
        } else {
            // get local or cached repo key
            String repoKey = item.getRepoKey();
            RepositoryService repositoryService = ContextHelper.get().getRepositoryService();
            FolderInfo folderInfo = new FolderInfo();
            CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
            AuthorizationService authService = ContextHelper.get().getAuthorizationService();
            RepoPath repoPath = InternalRepoPathFactory.create(repoKey, item.getRelativePath());
            folderInfo.populateFolderInfo(repositoryService, repoPath, centralConfig, authService.currentUsername());
            this.info = folderInfo;
        }
    }

    /***
     * @param item
     * @return
     */
    private BaseInfo populateVirtualRemoteFolderInfo(BaseBrowsableItem item) {
        FolderInfo repoInfo = new FolderInfo();
        repoInfo.populateVirtualRemoteFolderInfo(item);
        return repoInfo;
    }

    public BaseInfo getInfo() {
        return info;
    }

    public void setInfo(BaseInfo info) {
        this.info = info;
    }

    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
