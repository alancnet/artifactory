package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.FolderInfo;
import org.artifactory.util.HttpUtils;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author Chen Keinan
 */
@JsonTypeName("folder")
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"repoKey", "path"})
public class FolderGeneralArtifactInfo extends GeneralArtifactInfo implements RestGeneralTab {

    private int artifactsCount;

    public FolderGeneralArtifactInfo(String name) {
        super(name);
    }

    FolderGeneralArtifactInfo(){}

    @Override
    public void populateGeneralData(ArtifactoryRestRequest artifactoryRestRequest, AuthorizationService authService) {
        RepoPath repoPath = retrieveRepoPath();
        CentralConfigService centralConfigService = retrieveCentralConfigService();
        RepositoryService repoService = retrieveRepoService();
        BaseInfo baseInfo = populateFolderInfo(repoService, repoPath, centralConfigService, authService);
        // populate folder info
        super.setInfo(baseInfo);
        // populate virtual repositories
        super.populateVirtualRepositories(HttpUtils.getServletContextUrl(artifactoryRestRequest.getServletRequest()));
    }

    /**
     * populate folder info data
     * @param repoService  -repository service
     * @param repoPath - repo path
     * @param centralConfigService - central config service
     * @return folder info instance
     */
    private BaseInfo populateFolderInfo(RepositoryService repoService,RepoPath repoPath,
            CentralConfigService centralConfigService, AuthorizationService authService) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.populateFolderInfo(repoService, repoPath, centralConfigService, authService.currentUsername());
        return folderInfo;
    }

    public int getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(int artifactsCount) {
        this.artifactsCount = artifactsCount;
    }

    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }
}
