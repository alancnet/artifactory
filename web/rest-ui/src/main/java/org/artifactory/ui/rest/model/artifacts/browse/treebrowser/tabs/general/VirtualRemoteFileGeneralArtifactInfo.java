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
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.FileInfo;

/**
 * @author Chen Keinan
 */
public class VirtualRemoteFileGeneralArtifactInfo extends BaseArtifactInfo {

    private BaseInfo info;

    public VirtualRemoteFileGeneralArtifactInfo() {
    }

    public VirtualRemoteFileGeneralArtifactInfo(String name) {
        super(name);
    }

    /***
     * @param item
     * @return
     */
    public void populateVirtualRemoteFileInfo(BaseBrowsableItem item) {
        FileInfo fileInfo = new FileInfo();
        if (item instanceof VirtualBrowsableItem || item.isRemote()) {
            fileInfo.populateVirtualRemoteFileInfo(item);
            this.info = fileInfo;
        } else {
            // get local or cached repo key
            String repoKey = item.getRepoKey();
            RepositoryService repoService = ContextHelper.get().getRepositoryService();
            RepoPath repoPath = InternalRepoPathFactory.create(repoKey, item.getRelativePath());
            CentralConfigService centralConfigService = ContextHelper.get().getCentralConfig();
            AuthorizationService authService = ContextHelper.get().getAuthorizationService();
            fileInfo.populateFileInfo(repoService, repoPath, centralConfigService, authService, false);
            this.info = fileInfo;
        }
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
