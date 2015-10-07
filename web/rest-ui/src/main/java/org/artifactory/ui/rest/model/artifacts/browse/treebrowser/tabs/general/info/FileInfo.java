package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info;

import com.google.common.collect.Sets;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.blackduck.BlackDuckAddon;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses.GeneralTabLicenseModel;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@JsonPropertyOrder(
    {
        "name", "repositoryPath",
        "moduleID","deployedBy", "size", "created", "lastModified", "licenses", "downloaded",
        "lastDownloadedBy", "lastDownloaded", "remoteDownloaded", "lastRemoteDownloadedBy", "lastRemoteDownloaded",
        "watchingSince", "showFilteredResourceCheckBox","filtered"
    }
)
public class FileInfo extends BaseInfo {

    private String moduleID;
    private String deployedBy;
    private String size;
    private String created;
    private String lastModified;
    Set<GeneralTabLicenseModel> licenses = Sets.newHashSet();

    private Long downloaded;
    private String lastDownloaded;
    private String lastDownloadedBy;

    private Long remoteDownloaded;
    private String lastRemoteDownloaded;
    private String lastRemoteDownloadedBy;

    private String watchingSince;
    private String lastReplicationStatus;
    private Boolean filtered;
    private Boolean showFilteredResourceCheckBox;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public Set<GeneralTabLicenseModel> getLicenses() {
        return licenses;
    }

    public void setLicenses(Set<GeneralTabLicenseModel> licenses) {
        this.licenses = licenses;
    }

    public Boolean getFiltered() {
        return filtered;
    }

    public void setFiltered(Boolean filtered) {
        this.filtered = filtered;
    }

    public Boolean getShowFilteredResourceCheckBox() {
        return showFilteredResourceCheckBox;
    }

    public void setShowFilteredResourceCheckBox(Boolean showFilteredResourceCheckBox) {
        this.showFilteredResourceCheckBox = showFilteredResourceCheckBox;
    }

    public Long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Long downloaded) {
        this.downloaded = downloaded;
    }

    public String getLastDownloaded() {
        return lastDownloaded;
    }

    public void setLastDownloaded(String lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    public String getLastDownloadedBy() {
        return lastDownloadedBy;
    }

    public void setLastDownloadedBy(String lastDownloadedBy) {
        this.lastDownloadedBy = lastDownloadedBy;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getWatchingSince() {
        return watchingSince;
    }

    public void setWatchingSince(String watchingSince) {
        this.watchingSince = watchingSince;
    }

    public String getLastReplicationStatus() {
        return lastReplicationStatus;
    }

    public void setLastReplicationStatus(String lastReplicationStatus) {
        this.lastReplicationStatus = lastReplicationStatus;
    }

    /**
     * populate File info data
     *
     * @param repoService          -repository service
     * @param repoPath             - repo path
     * @param centralConfigService - central config service
     */
    public void populateFileInfo(RepositoryService repoService, RepoPath repoPath,
                                 CentralConfigService centralConfigService, AuthorizationService authService,
                                 boolean isBlackDuckEnabled) {
        // update name
        this.setName(repoPath.getName());
        // update path
        this.setRepositoryPath(repoPath.getRepoKey()+"/"+repoPath.getPath());
        ItemInfo itemInfo = repoService.getItemInfo(repoPath);
        // update file info created
        updateFileInfoCreated(centralConfigService, itemInfo);
        // update deployed by
        this.setDeployedBy(itemInfo.getModifiedBy());
        // update last modified
        updateFileInfoLastModified(centralConfigService, itemInfo);
        // update size
        this.setSize(StorageUnit.toReadableString(((org.artifactory.fs.FileInfo) itemInfo).getSize()));
        // update file filtered module id
        updateFileInfoModuleID(repoService, repoPath);
        // update filtered file info
        updateFilteredResourceInfo(repoPath, authService);
        // update file info stats
        updateFileInfoStats(repoService, repoPath, centralConfigService);
        // update file info licenses
        updateFileInfoLicenses(repoPath, itemInfo, isBlackDuckEnabled);
        // set watching since
        setWatchingSince(fetchWatchingSince(authService.currentUsername(), repoPath));
        // set last replication status
        setLastReplicationStatus(getLastReplicationInfo(repoPath));
    }

    /**
     * update file info statistics data
     *
     * @param repoService          - repository service
     * @param repoPath             - repository path
     * @param centralConfigService - central config service
     */
    private void updateFileInfoStats(RepositoryService repoService, RepoPath repoPath,
                                     CentralConfigService centralConfigService) {
        StatsInfo statsInfo = repoService.getStatsInfo(repoPath);
        if (statsInfo == null) {
            statsInfo = InfoFactoryHolder.get().createStats();
        }

        // local stats
        this.setDownloaded(statsInfo.getDownloadCount());
        this.setLastDownloadedBy(statsInfo.getLastDownloadedBy());
        if (statsInfo.getLastDownloaded() != 0) {
            String lastDownloadedString = centralConfigService.format(statsInfo.getLastDownloaded());
            this.setLastDownloaded(lastDownloadedString);
        }


        // disable smart repo stats for release 4.1

       /* // remote stats
        this.setRemoteDownloaded(statsInfo.getRemoteDownloadCount());
        this.setLastRemoteDownloadedBy(statsInfo.getRemoteLastDownloadedBy());
        if (statsInfo.getRemoteLastDownloaded() != 0) {
            String lastRemoteDownloadedString = centralConfigService.format(statsInfo.getRemoteLastDownloaded());
            this.setLastRemoteDownloaded(lastRemoteDownloadedString);
        }*/
    }

    /**
     * update fine info created
     *
     * @param centralConfigService - central configuration service
     * @param itemInfo             - item info
     */
    private void updateFileInfoCreated(CentralConfigService centralConfigService, ItemInfo itemInfo) {
        String created = centralConfigService.format(itemInfo.getCreated());
        this.setCreated(created);
    }

    /**
     * update fine info created
     *
     * @param centralConfigService - central configuration service
     * @param itemInfo             - item info
     */
    private void updateFileInfoLastModified(CentralConfigService centralConfigService, ItemInfo itemInfo) {
        String created = centralConfigService.format(itemInfo.getLastModified());
        this.setLastModified(created);
    }

    /**
     * update file info module id
     *
     * @param repoService - repository service
     * @param repoPath    - repository path
     */
    private void updateFileInfoModuleID(RepositoryService repoService, RepoPath repoPath) {
        ModuleInfo moduleInfo = repoService.getItemModuleInfo(repoPath);
        String moduleID;
        if (moduleInfo.isValid()) {
            moduleID = moduleInfo.getPrettyModuleId();
        } else {
            moduleID = "N/A";
        }
        this.setModuleID(moduleID);
    }

    /**
     * update file filtered info
     */
    private void updateFilteredResourceInfo(RepoPath path, AuthorizationService authService) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        if (authService.canAnnotate(path) && addonsManager.isAddonSupported(AddonType.FILTERED_RESOURCES)) {
            showFilteredResourceCheckBox = true;
            filtered = addonsManager.addonByType(FilteredResourcesAddon.class).isFilteredResourceFile(path);
        } else {
            showFilteredResourceCheckBox = false;
        }
    }

    /**
     * Returns a list of all licenses set as properties on this path, including black duck licenses if available
     */
    private void updateFileInfoLicenses(RepoPath path, ItemInfo itemInfo, boolean isBlackDuckEnabled) {
        boolean hasLicenses = false;
        if (itemInfo.isFolder()) {
            return;
        }
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        if (isBlackDuckEnabled) {
            Set<String> pathLicensesFromProperties = addonsManager.addonByType(
                    BlackDuckAddon.class).getPathLicensesFromProperties(path);
            if (pathLicensesFromProperties != null) {
                hasLicenses = true;
                List<GeneralTabLicenseModel> licenseModels = pathLicensesFromProperties
                        .stream()
                        .map(GeneralTabLicenseModel::blackDuckOf)
                        .collect(Collectors.toList());
                licenses.addAll(licenseModels);

            }
        } else {
            Set<LicenseInfo> pathLicensesByProps = addonsManager.addonByType(
                    LicensesAddon.class).getPathLicensesByProps(path);
            if (pathLicensesByProps != null) {
                hasLicenses = true;
                licenses.addAll(pathLicensesByProps
                        .stream()
                        .map(GeneralTabLicenseModel::new)
                        .collect(Collectors.toList()));

                //Remove not found - UI gets an empty array and handles.
                licenses.remove(GeneralTabLicenseModel.NOT_FOUND);
            }
        }
        if (!hasLicenses) {
            licenses = null;
        }
    }

    /**
     * populate remote / virtual file info
     *
     * @param item - remote virtual browsable item
     */
    public void populateVirtualRemoteFileInfo(BaseBrowsableItem item) {
        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        // update name
        this.setName(item.getName());
        // update path
        this.setRepositoryPath(item.getRepoKey() + "/" + item.getRelativePath());
        // init licenses to null not require for remove and virtual
        licenses = null;
        if (!item.isRemote()) {
            this.setCreated(centralConfig.format(item.getCreated()));
            this.setLastModified(centralConfig.format(item.getLastModified()));
            this.setSize(StorageUnit.toReadableString(item.getSize()));
        }
    }

    public Long getRemoteDownloaded() {
        return remoteDownloaded;
    }

    public void setRemoteDownloaded(Long remoteDownloaded) {
        this.remoteDownloaded = remoteDownloaded;
    }

    public String getLastRemoteDownloaded() {
        return lastRemoteDownloaded;
    }

    public void setLastRemoteDownloaded(String lastRemoteDownloaded) {
        this.lastRemoteDownloaded = lastRemoteDownloaded;
    }

    public String getLastRemoteDownloadedBy() {
        return lastRemoteDownloadedBy;
    }

    public void setLastRemoteDownloadedBy(String lastRemoteDownloadedBy) {
        this.lastRemoteDownloadedBy = lastRemoteDownloadedBy;
    }
}
