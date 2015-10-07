package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * @author Chen Keinan
 */
@JsonPropertyOrder(
    {
        "name", "repoType", "repositoryPath",
        "repositoryLayout","description", "artifactsCount", "created", "watchingSince", "lastReplicationStatus",
        "signingKeyLink"
    }
)
public class RepositoryInfo extends BaseInfo {
    private String remoteRepoUrl;
    private String repositoryLayout;
    private String description;
    private int artifactsCount;
    private String created;
    private String watchingSince;
    private String lastReplicationStatus;
    private String signingKeyLink;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepositoryLayout() {
        return repositoryLayout;
    }

    public void setRepositoryLayout(String repositoryLayout) {
        this.repositoryLayout = repositoryLayout;
    }

    public String getSigningKeyLink() {
        return signingKeyLink;
    }

    public void setSigningKeyLink(String signingKeyLink) {
        this.signingKeyLink = signingKeyLink;
    }

    public int getArtifactsCount() {
        return artifactsCount;
    }

    public void setArtifactsCount(int artifactsCount) {
        this.artifactsCount = artifactsCount;
    }

    /**
     * populate Repository info data
     * @param repoService  -repository service
     * @param repoDescriptor - repo descriptor
     * @param repoPath - repo path
     * @param centralConfigService - central config service
     * @return
     */
    public void populateRepositoryInfo(RepositoryService repoService ,
            LocalRepoDescriptor repoDescriptor,RepoPath repoPath,
            CentralConfigService centralConfigService, String userName) {

        // set repository description
        setRepositoryDescription(repoDescriptor);
        // repository name
        this.setName(repoPath.getRepoKey());
        // set repository path
        this.setRepositoryPath(repoPath.getRepoKey() + SLASH);
        // set repository layout
        setRepositoryLayout(repoDescriptor);
        // set created since data
        setCreatedSinceData(repoService, repoPath, centralConfigService, userName);
        // set last replication status
        setLastReplicationStatus(getLastReplicationInfo(repoPath));

        setRepoType(repoDescriptor.getType().name());
    }

    /**
     * populate Repository info data
     *
     * @param repoDescriptor - repo descriptor
     * @param repoPath       - repo path
     * @return
     */
    public void populateVirtualRepositoryInfo(RepoBaseDescriptor repoDescriptor, RepoPath repoPath) {
        // set repository description
        setRepositoryDescription(repoDescriptor);
        // repository name
        this.setName(repoPath.getRepoKey());
        // set repository path
        this.setRepositoryPath(repoPath.getRepoKey() + SLASH);
        // set repository layout
        setRepositoryLayout(repoDescriptor);

        setRepoType(repoDescriptor.getType().name());
    }

    /**
     * populate Repository info data
     *
     * @param repoDescriptor - repo descriptor
     * @param repoPath       - repo path
     * @return
     */
    public void populateRemoteRepositoryInfo(RepoBaseDescriptor repoDescriptor, RepoPath repoPath) {
        // set repository description
        setRepositoryDescription(repoDescriptor);
        // repository name
        this.setName(repoPath.getRepoKey());
        // set repository path
        this.setRepositoryPath(repoPath.getRepoKey() + SLASH);
        // set repository url
        setRemoteRepositoryUrl(repoDescriptor);

        setRepoType(repoDescriptor.getType().name());

        setIsSmart(repoDescriptor);
    }

    private void setIsSmart(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null) {
            this.setSmartRepo(((RemoteRepoDescriptor) repoDescriptor)
                    .getContentSynchronisation().isEnabled());
        }
    }

    private void setRemoteRepositoryUrl(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null) {
            this.setRemoteRepoUrl(((RemoteRepoDescriptor) repoDescriptor).getUrl());
        }
    }

    private void setRepositoryDescription(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null) {
            this.setDescription(repoDescriptor.getDescription());
        }
    }

    private void setRepositoryLayout(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor.getRepoLayout() != null) {
            this.setRepositoryLayout(repoDescriptor.getRepoLayout().getName());
        }
    }

    /**
     * set created since data
     *
     * @param repoService          - repository service
     * @param repoPath             - repository path
     * @param centralConfigService - central config service
     * @param userName             - user name
     */
    private void setCreatedSinceData(RepositoryService repoService, RepoPath repoPath, CentralConfigService centralConfigService, String userName) {
        ItemInfo itemInfo = repoService.getItemInfo(repoPath);
        setWatchingSince(fetchWatchingSince(userName, repoPath));
        String created = centralConfigService.format(itemInfo.getCreated());
        this.setCreated(created);
    }

    public String getRemoteRepoUrl() {
        return remoteRepoUrl;
    }

    public void setRemoteRepoUrl(String remoteRepoUrl) {
        this.remoteRepoUrl = remoteRepoUrl;
    }

}
