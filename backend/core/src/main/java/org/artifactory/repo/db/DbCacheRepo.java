/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.repo.db;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.cache.expirable.CacheExpiry;
import org.artifactory.repo.cache.expirable.ZapItemVisitor;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapter;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.RepoRequests;
import org.artifactory.request.Request;
import org.artifactory.resource.ExpiredRepoResource;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.sapi.fs.VfsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A remote cache repository implementation backed by db.
 *
 * @author Yossi Shaul
 */
public class DbCacheRepo extends DbLocalRepo<LocalCacheRepoDescriptor> implements LocalCacheRepo {
    private static final Logger log = LoggerFactory.getLogger(DbCacheRepo.class);

    private final RemoteRepo<? extends RemoteRepoDescriptor> remoteRepo;

    public DbCacheRepo(RemoteRepo<? extends RemoteRepoDescriptor> remoteRepo, DbCacheRepo oldCacheRepo) {
        super(createCacheDescriptorFromRemote(remoteRepo.getDescriptor()), remoteRepo.getRepositoryService(),
                oldCacheRepo);
        this.remoteRepo = remoteRepo;
    }

    private static LocalCacheRepoDescriptor createCacheDescriptorFromRemote(RemoteRepoDescriptor remoteDescriptor) {
        // create descriptor on-the-fly since this repo is created by a remote repo
        LocalCacheRepoDescriptor descriptor = new LocalCacheRepoDescriptor();
        descriptor.setDescription(remoteDescriptor.getDescription() + " (local file cache)");
        descriptor.setKey(remoteDescriptor.getKey() + LocalCacheRepoDescriptor.PATH_SUFFIX);
        descriptor.setRemoteRepo(remoteDescriptor);
        descriptor.setRepoLayout(remoteDescriptor.getRepoLayout());
        return descriptor;
    }

    @Override
    public boolean isCache() {
        return true;
    }

    @Override
    public RepoResource getInfo(InternalRequestContext context) throws FileExpectedException {
        RepoResource repoResource = super.getInfo(context);
        if (repoResource.isFound()) {
            //Check for expiry
            RepoRequests.logToContext("Found the resource in the cache - checking for expiry");
            boolean forceDownloadIfNewer = false;
            Request request = context.getRequest();
            if (request != null) {
                String forcePropValue = request.getParameter(ArtifactoryRequest.PARAM_FORCE_DOWNLOAD_IF_NEWER);
                if (StringUtils.isNotBlank(forcePropValue)) {
                    forceDownloadIfNewer = Boolean.valueOf(forcePropValue);
                    RepoRequests.logToContext("Found request parameter {}=%s",
                            ArtifactoryRequest.PARAM_FORCE_DOWNLOAD_IF_NEWER, forceDownloadIfNewer);
                }
            }

            if (forceDownloadIfNewer || context.isForceExpiryCheck() || isExpired(repoResource)) {
                RepoRequests.logToContext("Returning resource as expired");
                repoResource = new ExpiredRepoResource(repoResource);
            } else {
                RepoRequests.logToContext("Returning cached resource");
            }
        }
        return repoResource;
    }

    @Override
    public void undeploy(RepoPath repoPath, boolean calcMavenMetadata) {
        // change from remote key to cache key and never calculate maven metadata on cache
        mixin.undeploy(new RepoPathImpl(this.getKey(), repoPath.getPath()), false);
    }

    @Override
    public RemoteRepo<? extends RemoteRepoDescriptor> getRemoteRepo() {
        return remoteRepo;
    }

    @Override
    public ChecksumPolicy getChecksumPolicy() {
        return remoteRepo.getChecksumPolicy();
    }

    @Override
    public RemoteRepoDescriptor getRemoteRepoDescriptor() {
        return remoteRepo.getDescriptor();
    }

    @Override
    public void unexpire(String path) {
        //Reset the resource age so it is kept being cached
        MutableVfsItem mutableItem = getMutableFsItem(getRepoPath(path));
        if (mutableItem != null) {
            mutableItem.setUpdated(System.currentTimeMillis());
        } else {
            log.error("Attempt to unexpire non existent resource: {}", path);
        }
    }

    @Override
    public int zap(RepoPath repoPath) {
        int itemsZapped = 0;
        //Zap all nodes recursively from all retrieval caches
        VfsItem fsItem = getImmutableFsItem(repoPath);
        if (fsItem != null) {
            // Exists and not deleted... Let's roll
            ZapItemVisitor zapVisitor = new ZapItemVisitor(this);
            zapVisitor.visit(fsItem);
            itemsZapped = zapVisitor.getUpdatedItemsCount();
            // now remove all the caches related to this path and any sub paths
            remoteRepo.removeFromCaches(fsItem.getPath(), true);
            log.info("Zapped '{}' from local cache: {} items zapped.", repoPath, itemsZapped);
        }
        return itemsZapped;
    }

    @Override
    public SnapshotVersionBehavior getMavenSnapshotVersionBehavior() {
        return getDescriptor().getSnapshotVersionBehavior();
    }

    @Override
    public boolean isSuppressPomConsistencyChecks() {
        return remoteRepo.getDescriptor().isSuppressPomConsistencyChecks();
    }

    @Override
    public MavenSnapshotVersionAdapter getMavenSnapshotVersionAdapter() {
        throw new UnsupportedOperationException("Local cache repositories doesn't have snapshot version adapter");
    }

    /**
     * Check that the item has not expired yet, unless it's a release which never expires or a unique snapshot.
     *
     * @param repoResource The resource to check for expiry
     * @return boolean - True if resource is expired. False if not
     */
    protected boolean isExpired(RepoResource repoResource) {
        String path = repoResource.getRepoPath().getPath();
        CacheExpiry cacheExpiry = ContextHelper.get().beanForType(CacheExpiry.class);
        if (cacheExpiry.isExpirable(this, path)) {
            long retrievalCachePeriodMillis = getRetrievalCachePeriodMillis(path);
            long cacheAge = repoResource.getCacheAge();
            return cacheAge > retrievalCachePeriodMillis || cacheAge == -1;
        }
        return false;
    }

    private long getRetrievalCachePeriodMillis(String path) {
        long retrievalCachePeriodMillis;
        if (MavenNaming.isIndex(path) &&
                remoteRepo.getUrl().contains(ConstantValues.mvnCentralHostPattern.getString())) {
            //If it is a central maven index use the hardcoded cache value
            long centralMaxQueryIntervalSecs = ConstantValues.mvnCentralIndexerMaxQueryIntervalSecs.getLong();
            retrievalCachePeriodMillis = centralMaxQueryIntervalSecs * 1000L;
        } else {
            //It is a non-unique snapshot or snapshot metadata
            retrievalCachePeriodMillis = remoteRepo.getRetrievalCachePeriodSecs() * 1000L;
        }
        return retrievalCachePeriodMillis;
    }
}
