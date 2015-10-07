package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.IgnoreSpecialFields;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.checksums.Checksums;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.dependecydeclaration.DependencyDeclaration;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.FileInfo;
import org.artifactory.util.HttpUtils;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author Chen Keinan
 */
@JsonTypeName("file")
@JsonFilter("exclude fields")
@IgnoreSpecialFields(value = {"repoKey", "path"})
public class FileGeneralArtifactInfo extends GeneralArtifactInfo implements RestGeneralTab {

    FileGeneralArtifactInfo() {
    }

    private DependencyDeclaration dependencyDeclaration;

    private Checksums checksums;

    private Boolean blackDuckEnabled;

    private boolean bintrayInfoEnabled;

    public FileGeneralArtifactInfo(String name) {
        super(name);
    }

    public Checksums getChecksums() {
        return checksums;
    }

    public void setChecksums(Checksums checksums) {
        this.checksums = checksums;
    }

    public void setDependencyDeclaration(DependencyDeclaration dependencyDeclaration) {
        this.dependencyDeclaration = dependencyDeclaration;
    }

    @Override
    public void populateGeneralData(ArtifactoryRestRequest artifactoryRestRequest, AuthorizationService authService) {
        RepoPath repoPath = retrieveRepoPath();
        CentralConfigService centralConfigService = retrieveCentralConfigService();
        RepositoryService repoService = retrieveRepoService();
        ItemInfo itemInfo = retrieveItemInfo(repoPath);
        LocalRepoDescriptor localRepoDescriptor = localOrCachedRepoDescriptor(repoPath);
        blackDuckEnabled = ContextHelper.get().beanForType(AddonsManager.class).addonByType(BlackDuckAddon.class)
                .isEnableIntegration();
        markIfBintrayIsEnabled(itemInfo);
        // populate file info data
        BaseInfo baseInfo = populateFileInfo(repoService, repoPath, centralConfigService, authService);
        super.setInfo(baseInfo);
        // update virtual repositories
        super.populateVirtualRepositories(HttpUtils.getServletContextUrl(artifactoryRestRequest.getServletRequest()));
        // update file info checksum
        updateFileInfoCheckSum((org.artifactory.fs.FileInfo) itemInfo, localRepoDescriptor, authService);
        //update Dependency Declaration
        updateDependencyDeclaration(artifactoryRestRequest, repoService, itemInfo, localRepoDescriptor);
    }

    private void markIfBintrayIsEnabled(ItemInfo itemInfo) {
        boolean hideInfo = ConstantValues.bintrayUIHideInfo.getBoolean();
        if (hideInfo) {
            bintrayInfoEnabled = false;
            return;
        }

        boolean validFile = isValidFile(itemInfo);
        if (!validFile) {
            bintrayInfoEnabled = false;
            return;
        }

        boolean offlineMode = retrieveCentralConfigService().getDescriptor().isOfflineMode();
        if (offlineMode) {
            bintrayInfoEnabled = false;
            return;
        }

        ModuleInfo moduleInfo = retrieveRepoService().getItemModuleInfo(itemInfo.getRepoPath());
        if (moduleInfo.isIntegration()) {
            bintrayInfoEnabled = false;
            return;
        }

        BintrayService bintrayService = ContextHelper.get().beanForType(BintrayService.class);
        boolean hasSystemAPIKey = bintrayService.hasBintraySystemUser();
        boolean userExists = isUserExists();
        if (!hasSystemAPIKey && !userExists) {
            bintrayInfoEnabled = false;
            return;
        }

        // All good, enable it
        bintrayInfoEnabled = true;
    }

    private boolean isValidFile(ItemInfo itemInfo) {
        return NamingUtils.isJarVariant(itemInfo.getName()) || NamingUtils.isPom(itemInfo.getName());
    }

    private boolean isUserExists() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
        return !addons.isAolAdmin() &&
                !ContextHelper.get().beanForType(UserGroupService.class).currentUser().isTransientUser();
    }

    /**
     * update dependency declaration
     *
     * @param artifactoryRestRequest - encapsulate data related to request
     * @param repoService            - repository service
     * @param itemInfo               - repository item info
     * @param localRepoDescriptor    - repository descriptor
     */
    private void updateDependencyDeclaration(ArtifactoryRestRequest artifactoryRestRequest,
                                             RepositoryService repoService, ItemInfo itemInfo,
                                             LocalRepoDescriptor localRepoDescriptor) {
        DependencyDeclaration localDependencyDeclaration = new DependencyDeclaration();
        localDependencyDeclaration.updateDependencyDeclaration(artifactoryRestRequest,
                repoService, itemInfo, localRepoDescriptor);
        if (localDependencyDeclaration.getTypes() != null) {
            dependencyDeclaration = localDependencyDeclaration;
        }
    }

    private void updateFileInfoCheckSum(org.artifactory.fs.FileInfo itemInfo, LocalRepoDescriptor localRepoDescriptor,
                                        AuthorizationService authService) {
        boolean hasPermissions = authService.canDeploy(itemInfo.getRepoPath()) && !authService.isAnonymous();
        checksums = new Checksums();
        checksums.updateFileInfoCheckSum(itemInfo, localRepoDescriptor, hasPermissions);
        checksums.updatePropertiesChecksums(itemInfo);
    }

    /**
     * populate File info data
     *
     * @param repoService          -repository service
     * @param repoPath             - repo path
     * @param centralConfigService - central config service
     * @return File info instance
     */
    private BaseInfo populateFileInfo(RepositoryService repoService, RepoPath repoPath,
                                      CentralConfigService centralConfigService, AuthorizationService authService) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.populateFileInfo(repoService, repoPath, centralConfigService, authService, blackDuckEnabled);
        return fileInfo;
    }

    public DependencyDeclaration getDependencyDeclaration() {
        return dependencyDeclaration;
    }

    public String toString() {
        return JsonUtil.jsonToStringIgnoreSpecialFields(this);
    }

    public Boolean getBlackDuckEnabled() {
        return blackDuckEnabled;
    }

    public boolean isBintrayInfoEnabled() {
        return bintrayInfoEnabled;
    }
}
