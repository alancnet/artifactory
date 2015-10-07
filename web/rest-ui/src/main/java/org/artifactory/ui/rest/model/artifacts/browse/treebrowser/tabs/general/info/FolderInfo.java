package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * @author Chen Keinan
 */
@JsonPropertyOrder(
    {
        "name", "repositoryPath",
        "deployedBy", "artifactsCount", "created", "watchingSince", "lastReplicationStatus"
    }
)
public class FolderInfo extends BaseInfo {

    private String deployedBy;
    private String created;
    private String watchingSince;
    private String lastReplicationStatus;
    private int artifactsCount;

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    /**
     * populate folder info data
     *
     * @param repoService          -repository service
     * @param repoPath             - repo path
     * @param centralConfigService - central config service
     * @return
     */
    public void populateFolderInfo(RepositoryService repoService, RepoPath repoPath,
                                   CentralConfigService centralConfigService, String userName) {
        // set name
        this.setName(repoPath.getName());
        // set repository path
        this.setRepositoryPath(repoPath.getRepoKey() + SLASH + repoPath.getPath() + SLASH);
        ItemInfo itemInfo = repoService.getItemInfo(repoPath);
        // set watching since
        setWatchingSince(fetchWatchingSince(userName, repoPath));
        // set created
        setCreated(centralConfigService, itemInfo);
        // set deployed by
        this.setDeployedBy(itemInfo.getModifiedBy());
        // set last replication status
        setLastReplicationStatus(getLastReplicationInfo(repoPath));
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

    public int getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(int artifactsCount) {
        this.artifactsCount = artifactsCount;
    }

    /**
     * @param item
     */
    public void populateVirtualRemoteFolderInfo(BaseBrowsableItem item) {
        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        // set name
        this.setName(item.getName());
        // set repository path
        this.setRepositoryPath(item.getRepoKey() + SLASH + item.getRelativePath() + SLASH);

        if (!item.isRemote()) {
            this.setCreated(centralConfig.format(item.getCreated()));
        }
    }

    /**
     * set created data
     *
     * @param centralConfigService - central configuration service
     * @param itemInfo             - item info
     */
    private void setCreated(CentralConfigService centralConfigService, ItemInfo itemInfo) {
        String created = centralConfigService.format(itemInfo.getCreated()) + " " + DurationFormatUtils.formatDuration(
                System.currentTimeMillis() - itemInfo.getCreated(), "(d'd' H'h' m'm' s's' ago)");
        this.setCreated(created);
    }

}
