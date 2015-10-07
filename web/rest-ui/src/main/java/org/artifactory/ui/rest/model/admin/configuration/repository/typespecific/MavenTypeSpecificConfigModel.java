package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class MavenTypeSpecificConfigModel implements TypeSpecificConfigModel {

    //local
    protected Integer maxUniqueSnapshots = DEFAULT_MAX_UNIQUE_SNAPSHOTS;
    protected Boolean handleReleases = DEFAULT_HANDLE_RELEASES;
    protected Boolean handleSnapshots = DEFAULT_HANDLE_SNAPSHOTS;
    protected Boolean suppressPomConsistencyChecks = DEFAULT_SUPPRESS_POM_CHECKS_MAVEN;
    protected SnapshotVersionBehavior snapshotVersionBehavior = DEFAULT_SNAPSHOT_BEHAVIOR;
    protected LocalRepoChecksumPolicyType localChecksumPolicy = DEFAULT_CHECKSUM_POLICY;

    //remote
    protected Boolean eagerlyFetchJars = DEFAULT_EAGERLY_FETCH_JARS;
    protected Boolean eagerlyFetchSources = DEFAULT_EAGERLY_FETCH_SOURCES;
    protected ChecksumPolicyType remoteChecksumPolicy = DEFAULT_REMOTE_CHECKSUM_POLICY;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE;
    protected Boolean rejectInvalidJars = DEFAULT_REJECT_INVALID_JARS;

    //virtual
    protected PomCleanupPolicy pomCleanupPolicy = DEFAULT_POM_CLEANUP_POLICY;
    protected String keyPair;

    public Integer getMaxUniqueSnapshots() {
        return maxUniqueSnapshots;
    }

    public void setMaxUniqueSnapshots(Integer maxUniqueSnapshots) {
        this.maxUniqueSnapshots = maxUniqueSnapshots;
    }

    public Boolean getHandleReleases() {
        return handleReleases;
    }

    public void setHandleReleases(Boolean handleReleases) {
        this.handleReleases = handleReleases;
    }

    public Boolean getHandleSnapshots() {
        return handleSnapshots;
    }

    public void setHandleSnapshots(Boolean handleSnapshots) {
        this.handleSnapshots = handleSnapshots;
    }

    public Boolean getSuppressPomConsistencyChecks() {
        return suppressPomConsistencyChecks;
    }

    public void setSuppressPomConsistencyChecks(Boolean suppressPomConsistencyChecks) {
        this.suppressPomConsistencyChecks = suppressPomConsistencyChecks;
    }

    public SnapshotVersionBehavior getSnapshotVersionBehavior() {
        return snapshotVersionBehavior;
    }

    public void setSnapshotVersionBehavior(SnapshotVersionBehavior snapshotVersionBehavior) {
        this.snapshotVersionBehavior = snapshotVersionBehavior;
    }

    public LocalRepoChecksumPolicyType getLocalChecksumPolicy() {
        return localChecksumPolicy;
    }

    public void setLocalChecksumPolicy(LocalRepoChecksumPolicyType localChecksumPolicy) {
        this.localChecksumPolicy = localChecksumPolicy;
    }

    public Boolean getEagerlyFetchJars() {
        return eagerlyFetchJars;
    }

    public void setEagerlyFetchJars(Boolean eagerlyFetchJars) {
        this.eagerlyFetchJars = eagerlyFetchJars;
    }

    public Boolean getEagerlyFetchSources() {
        return eagerlyFetchSources;
    }

    public void setEagerlyFetchSources(Boolean eagerlyFetchSources) {
        this.eagerlyFetchSources = eagerlyFetchSources;
    }

    public ChecksumPolicyType getRemoteChecksumPolicy() {
        return remoteChecksumPolicy;
    }

    public void setRemoteChecksumPolicy(ChecksumPolicyType remoteChecksumPolicy) {
        this.remoteChecksumPolicy = remoteChecksumPolicy;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    public Boolean getRejectInvalidJars() {
        return rejectInvalidJars;
    }

    public void setRejectInvalidJars(Boolean rejectInvalidJars) {
        this.rejectInvalidJars = rejectInvalidJars;
    }

    public void setPomCleanupPolicy(PomCleanupPolicy pomCleanupPolicy) {
        this.pomCleanupPolicy = pomCleanupPolicy;
    }

    public PomCleanupPolicy getPomCleanupPolicy() {
        return pomCleanupPolicy;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.Maven;
    }

    @Override
    public String getUrl() {
        return RepoConfigDefaultValues.MAVEN_GROUP_URL;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
