package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class NugetTypeSpecificConfigModel implements TypeSpecificConfigModel {

    protected Boolean forceNugetAuthentication = DEFAULT_FORCE_NUGET_AUTH;

    //local
    protected Integer maxUniqueSnapshots = DEFAULT_MAX_UNIQUE_SNAPSHOTS;

    //remote
    protected String feedContextPath = DEFAULT_NUGET_FEED_PATH;
    protected String downloadContextPath = DEFAULT_NUGET_DOWNLOAD_PATH;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE;

    public String getFeedContextPath() {
        return feedContextPath;
    }

    public void setFeedContextPath(String feedContextPath) {
        this.feedContextPath = feedContextPath;
    }

    public String getDownloadContextPath() {
        return downloadContextPath;
    }

    public void setDownloadContextPath(String downloadContextPath) {
        this.downloadContextPath = downloadContextPath;
    }

    public Integer getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(Integer maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public Boolean isForceNugetAuthentication() {
        return forceNugetAuthentication;
    }

    public void setForceNugetAuthentication(Boolean forceNugetAuthentication) {
        this.forceNugetAuthentication = forceNugetAuthentication;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.NuGet;
    }

    @Override
    public String getUrl() {
        return RepoConfigDefaultValues.NUGET_URL;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
