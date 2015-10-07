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

package org.artifactory.repo;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.RestCoreAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.plugin.ResourceStreamCtx;
import org.artifactory.addon.plugin.download.AltRemoteContentAction;
import org.artifactory.addon.plugin.download.AltRemotePathAction;
import org.artifactory.addon.plugin.download.PathCtx;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusHolder;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.RemoteResourceStreamHandle;
import org.artifactory.io.SimpleResourceStreamHandle;
import org.artifactory.io.checksum.Checksum;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.io.checksum.policy.ChecksumPolicyBase;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.db.DbCacheRepo;
import org.artifactory.repo.local.ValidDeployPathContext;
import org.artifactory.repo.remote.browse.RemoteItem;
import org.artifactory.repo.remote.interceptor.RemoteRepoInterceptor;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.NullRequestContext;
import org.artifactory.request.RemoteRequestException;
import org.artifactory.request.RepoRequests;
import org.artifactory.request.Request;
import org.artifactory.request.RequestContext;
import org.artifactory.resource.RemoteRepoResource;
import org.artifactory.resource.RepoResourceInfo;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.resource.UnfoundRepoResourceReason;
import org.artifactory.resource.UnfoundRepoResourceReason.Reason;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.storage.fs.lock.map.LockingMap;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.UploadEntry;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.HttpClientUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yoavl
 */
public abstract class RemoteRepoBase<T extends RemoteRepoDescriptor> extends RealRepoBase<T> implements RemoteRepo<T> {
    private static final Logger log = LoggerFactory.getLogger(RemoteRepoBase.class);
    private final ChecksumPolicy checksumPolicy;
    private final LockingMap lockingMap;
    /**
     * Flags this repository as assumed offline. The repository enters this state when a download request fails with
     * exception.
     */
    protected volatile boolean assumedOffline;

    /**
     * The next time, in milliseconds, to check online status of this repository
     */
    protected long nextOnlineCheckMillis;
    private LocalCacheRepo localCacheRepo;
    private RemoteRepoBase oldRemoteRepo;
    /**
     * Cache of resources not found on the remote machine. Keyed by resource path.
     */
    private Map<String, RepoResource> missedRetrievalsCache;
    /**
     * Cache of remote directories listing.
     */
    private Map<String, List<RemoteItem>> remoteResourceCache;
    private boolean globalOfflineMode;
    // List of interceptors for various download resolution points
    private Collection<RemoteRepoInterceptor> interceptors;

    protected RemoteRepoBase(T descriptor, InternalRepositoryService repositoryService,
            boolean globalOfflineMode,RemoteRepo oldRemoteRepo) {
        super(descriptor, repositoryService);
        ChecksumPolicyType checksumPolicyType = descriptor.getChecksumPolicyType();
        checksumPolicy = ChecksumPolicyBase.getByType(checksumPolicyType);
        this.globalOfflineMode = globalOfflineMode;
        if (oldRemoteRepo instanceof RemoteRepoBase) {
            this.oldRemoteRepo = (RemoteRepoBase) oldRemoteRepo;
            // Always keep the in transit download map
            this.lockingMap = this.oldRemoteRepo.lockingMap;
        } else {
            this.oldRemoteRepo = null;
            HaAddon haAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaAddon.class);
            this.lockingMap = haAddon.getLockingMap();
        }
        if (isNonMavenRepo()) {
            excludes.addAll(Lists.newArrayList("**/*.pom", "**/*.jar", "**/maven-metadata.xml"));
        }
    }

    @Override
    public void init() {
        if (isStoreArtifactsLocally()) {
            DbCacheRepo oldCacheRepo = null;
            if (oldRemoteRepo != null) {
                oldCacheRepo = (DbCacheRepo) oldRemoteRepo.localCacheRepo;
            }
            //Initialize the local cache
            localCacheRepo = new DbCacheRepo(this, oldCacheRepo);
            localCacheRepo.init();
        }
        initCaches();
        logCacheInfo();
        // Clean the old repo not needed anymore
        oldRemoteRepo = null;
        interceptors = ContextHelper.get().beansForType(RemoteRepoInterceptor.class).values();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (isStoreArtifactsLocally()) {
            localCacheRepo.destroy();
        }
    }

    protected void initCaches() {
        missedRetrievalsCache = initCache(500, getDescriptor().getMissedRetrievalCachePeriodSecs(), false);
        remoteResourceCache = initCache(1000, getDescriptor().getRetrievalCachePeriodSecs(), true);
    }

    private <V> Map<String, V> initCache(int initialCapacity, long expirationSeconds, boolean softValues) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder().initialCapacity(initialCapacity);
        if (expirationSeconds >= 0) {
            cacheBuilder.expireAfterWrite(expirationSeconds, TimeUnit.SECONDS);
        }
        if (softValues) {
            cacheBuilder.softValues();
        }
        //noinspection unchecked
        return cacheBuilder.build().asMap();
    }

    private void logCacheInfo() {
        long retrievalCachePeriodSecs = getDescriptor().getRetrievalCachePeriodSecs();
        if (retrievalCachePeriodSecs > 0) {
            log.debug("{}: Retrieval cache will be enabled with period of {} seconds",
                    this, retrievalCachePeriodSecs);
        } else {
            log.debug("{}: Retrieval cache will be disabled.", this);
        }
        long missedRetrievalCachePeriodSecs = getDescriptor().getMissedRetrievalCachePeriodSecs();
        if (missedRetrievalCachePeriodSecs > 0) {
            log.debug("{}: Enabling misses retrieval cache with period of {} seconds",
                    this, missedRetrievalCachePeriodSecs);
        } else {
            log.debug("{}: Disabling misses retrieval cache", this);
        }
    }

    @Override
    public boolean isStoreArtifactsLocally() {
        return getDescriptor().isStoreArtifactsLocally();
    }

    @Override
    public String getUrl() {
        return getDescriptor().getUrl();
    }

    @Override
    public boolean isOffline() {
        return getDescriptor().isOffline() || globalOfflineMode || isAssumedOffline();
    }

    @Override
    public boolean isAssumedOffline() {
        return assumedOffline;
    }

    @Override
    public long getNextOnlineCheckMillis() {
        return isAssumedOffline() ? nextOnlineCheckMillis : 0;
    }

    @Override
    public boolean isListRemoteFolderItems() {
        return getDescriptor().isListRemoteFolderItems() && !getDescriptor().isBlackedOut() && !isOffline();
    }

    @Override
    public long getRetrievalCachePeriodSecs() {
        return getDescriptor().getRetrievalCachePeriodSecs();
    }

    @Override
    public ChecksumPolicy getChecksumPolicy() {
        return checksumPolicy;
    }

    /**
     * Retrieve the (metadata) information about the artifact, unless still cached as failure or miss. Reach this point
     * only if local and cached repo did not find resource or expired.
     *
     * @param context The request context holding additional parameters
     * @return A repository resource updated with the uptodate metadata
     */
    @Override
    public final RepoResource getInfo(InternalRequestContext context) throws FileExpectedException {
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        RestCoreAddon restCoreAddon = addonsManager.addonByType(RestCoreAddon.class);
        context = restCoreAddon.getDynamicVersionContext(this, context, true);

        String path = context.getResourcePath();
        // make sure the repo key is of this repository
        RepoPath repoPath = InternalRepoPathFactory.create(getKey(), path);

        //Skip if in blackout or not accepting/handling or cannot download
        StatusHolder statusHolder = checkDownloadIsAllowed(repoPath);
        if (statusHolder.isError()) {
            RepoRequests.logToContext("Download denied (%s) - returning unfound resource", statusHolder.getStatusMsg());
            return new UnfoundRepoResource(repoPath, statusHolder.getStatusMsg(), statusHolder.getStatusCode());
        }
        //Never query remote checksums
        if (NamingUtils.isChecksum(path)) {
            RepoRequests.logToContext("Download denied - checksums are not downloadable");
            return new UnfoundRepoResource(repoPath, "Checksums are not downloadable.");
        }

        //Try to get it from the caches
        RepoResource res = getMissedResource(path);
        if (res == null) {
            res = internalGetInfo(repoPath, context);
        }

        //If we cannot get the resource remotely and an expired (otherwise we would not be
        //attempting the remote repo at all) cache entry exists use it by unexpiring it
        if (res.isExpired() && isStoreArtifactsLocally()) {
            RepoRequests.logToContext("Hosting repository stores locally and the resource is expired - " +
                    "un-expiring if still exists");
            res = getRepositoryService().unexpireIfExists(localCacheRepo, path);
        }
        checkAndMarkExpirableResource(res);
        return res;
    }

    private RepoResource internalGetInfo(RepoPath repoPath, InternalRequestContext context) {
        String path = repoPath.getPath();
        RepoResource cachedResource = null;
        // first try to get it from the local cache repository
        if (isStoreArtifactsLocally() && localCacheRepo != null) {
            try {
                cachedResource = localCacheRepo.getInfo(context);
            } catch (FileExpectedException e) {
                // rethrow using the remote repo path
                throw new FileExpectedException(repoPath);
            }
        }

        if (cachedResource != null) {
            if (cachedResource.isFound()) {
                RepoRequests.logToContext("Found resource in local cache - returning cached resource");
                // found in local cache
                return returnCachedResource(repoPath, cachedResource);
            } else if (cachedResource instanceof UnfoundRepoResourceReason &&
                    ((UnfoundRepoResourceReason)cachedResource).getReason() == Reason.PROPERTY_MISMATCH) {
                RepoRequests.logToContext("Found resource in local cache, but property doesn't match.");
                return returnCachedResource(repoPath, cachedResource);
            }
        }

        boolean foundExpiredInCache = ((cachedResource != null) && cachedResource.isExpired());

        //not found in local cache - try to get it from the remote repository
        if (!isOffline()) {
            boolean remoteDownloadAllowed = isRemoteDownloadAllowed(repoPath);
            if (!remoteDownloadAllowed) {
                RepoRequests.logToContext("Remote resource not allowed based on one of the interceptors");
                return new UnfoundRepoResource(repoPath,
                        "Remote download of " + repoPath + " is not allowed by this repo");
            }

            RepoResource remoteResource = getRemoteResource(context, repoPath, foundExpiredInCache);
            if (!remoteResource.isFound() && foundExpiredInCache) {
                RepoRequests.logToContext("Resource doesn't exist remotely but is expired in the caches - " +
                        "returning expired cached resource");
                remoteResource = returnCachedResource(repoPath, cachedResource);
            }
            if (remoteResource.isFound() && context.getRequest().isZipResourceRequest()) {
                // there's a newer remote resource that should be downloaded for zip resources
                return getRemoteZipRepoResource(repoPath, context);
            }
            return remoteResource;
        } else if (foundExpiredInCache) {
            RepoRequests.logToContext("Repository is offline but the resource exists in the local cache - " +
                    "returning cached resource");
            //Return the cached resource if remote fetch failed
            return returnCachedResource(repoPath, cachedResource);
        } else {
            String offlineMessage = isAssumedOffline() ? "assumed offline" : "offline";
            RepoRequests.logToContext("Repository is " + offlineMessage + " and the resource doesn't exist in the " +
                    "local cache - returning unfound resource");
            return new UnfoundRepoResource(repoPath,
                    String.format("%s: is %s, '%s' is not found at '%s'.", this, offlineMessage, repoPath, path));
        }
    }

    private RepoResource getRemoteZipRepoResource(RepoPath repoPath, InternalRequestContext context) {
        try {
            RepoRequests.logToContext(
                    "Main ZIP resource {} exist remotely but is expired or is not present in the caches - " +
                            "doing eager download.", repoPath
            );
            EagerResourcesDownloader eagerResourcesDownloader = InternalContextHelper.get().beanForType(
                    EagerResourcesDownloader.class);
            InternalArtifactoryRequest internalRequest = new InternalArtifactoryRequest(repoPath);
            String alternativeDownloadUrl = context.getRequest().getParameter(
                    ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL);
            if (StringUtils.isNotBlank(alternativeDownloadUrl)) {
                internalRequest.setAlternativeRemoteDownloadUrl(alternativeDownloadUrl);
            }
            eagerResourcesDownloader.downloadNow(repoPath, internalRequest);
            RepoResource cachedResource = localCacheRepo.getInfo(context);
            if (cachedResource != null && cachedResource.isFound()) {
                RepoRequests.logToContext(
                        "Found resource after eager download in local cache - returning cached resource");
                return returnCachedResource(repoPath, cachedResource);
            }
            String zipResourcePath = context.getRequest().getZipResourcePath();
            return new UnfoundRepoResource(repoPath, "Could not download '" + zipResourcePath + "' from main zip resource");
        } catch (Exception e) {
            RepoRequests.logToContext(
                    "Main ZIP resource exists remotely but could not be downloaded due to: {}", e.getMessage());
            log.warn("Main ZIP resource {} exists remotely but could not be downloaded due to: {}",
                    repoPath, e.getMessage());
            if (log.isDebugEnabled()) {
                log.warn(e.getMessage(), e);
            }
            return new UnfoundRepoResource(repoPath, "Zip resources download failed due to: " + e.getMessage());
        }
    }

    /**
     * Returns a resource from a remote repository
     *
     * @param context             Download request context
     * @param repoPath            Item repo path
     * @param foundExpiredInCache True if the an expired item was found in the cache    @return Repo resource object
     */
    private RepoResource getRemoteResource(RequestContext context, RepoPath repoPath, boolean foundExpiredInCache) {
        String path = repoPath.getPath();
        boolean folder = repoPath.isFolder();
        if (!isSynchronizeProperties() && context.getProperties().hasMandatoryProperty()) {
            RepoRequests.logToContext("Repository doesn't sync properties and the request contains " +
                    "mandatory properties - returning unfound resource");
            return new UnfoundRepoResource(repoPath, this + ": does not synchronize remote properties and request " +
                    "contains mandatory property, '" + repoPath + "' will not be downloaded from '" + path + "'.");
        }

        RepoResource remoteResource;
        path = getAltRemotePath(repoPath);
        if (!repoPath.getPath().equals(path)) {
            RepoRequests.logToContext("Remote resource path was altered by the user plugins to - %s", path);
        }
        try {
            remoteResource = retrieveInfo(path, folder, context);
            if (!remoteResource.isFound() && !foundExpiredInCache) {
                //Update the non-found cache for a miss
                RepoRequests.logToContext("Unable to find resource remotely - adding to the missed retrieval cache.");
                missedRetrievalsCache.put(path, remoteResource);
            }
        } catch (FileExpectedException e) {
            RepoRequests.logToContext("Expected file but got directory, requesting a redirect");
            throw e;
        } catch (Exception e) {
            RepoRequests.logToContext("Failed to retrieve information: %s", e.getMessage());
            String reason = this + ": Error in getting information for '" + path + "' (" + e.getMessage() + ").";
            if (log.isDebugEnabled()) {
                log.warn(reason, e);
            } else {
                log.warn(reason);
            }
            putOffline();
            remoteResource = new UnfoundRepoResource(repoPath, reason);
            if (!foundExpiredInCache && getDescriptor().isHardFail()) {
                throw new RuntimeException(this + ": Error in getting information for '" + path + "'.", e);
            }
        }

        return remoteResource;
    }

    /**
     * Sets the response repo path on a cached resource
     *
     * @param repoPath       Path item to resource
     * @param cachedResource Cached resource
     * @return Repo resource object
     */
    private RepoResource returnCachedResource(RepoPath repoPath, RepoResource cachedResource) {
        cachedResource.setResponseRepoPath(InternalRepoPathFactory.create(localCacheRepo.getKey(), repoPath.getPath()));
        return cachedResource;
    }

    /**
     * Temporarily puts the repository in an assumed offline mode.
     */
    protected abstract void putOffline();

    protected abstract RepoResource retrieveInfo(String path, boolean folder, @Nullable RequestContext context);

    @Override
    public StatusHolder checkDownloadIsAllowed(RepoPath repoPath) {
        String path = repoPath.getPath();
        BasicStatusHolder status = assertValidPath(repoPath, true);
        if (status.isError()) {
            return status;
        }

        // permissions are always on the cache repo key
        RepoPath cacheRepoPath = InternalRepoPathFactory.create(getKey() + LocalCacheRepoDescriptor.PATH_SUFFIX, path);
        if (localCacheRepo != null) {
            return localCacheRepo.checkDownloadIsAllowed(cacheRepoPath);
        } else {
            // cache repo doesn't exist so remote has to check the permissions
            assertReadPermissions(cacheRepoPath, status);
            return status;
        }
    }

    @Override
    public ResourceStreamHandle getResourceStreamHandle(InternalRequestContext requestContext, RepoResource res)
            throws IOException, RepoRejectException {
        // We also change the context here, otherwise if there is something in the cache
        // we will receive it instead of trying to download the latest from the remote
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        RestCoreAddon restCoreAddon = addonsManager.addonByType(RestCoreAddon.class);
        requestContext = restCoreAddon.getDynamicVersionContext(this, requestContext, true);
        RepoRequests.logToContext("Creating a resource handle from '%s'", res.getResponseRepoPath().getRepoKey());
        String path = res.getRepoPath().getPath();
        if (isStoreArtifactsLocally()) {
            RepoRequests.logToContext("Target repository is configured to retain artifacts locally - " +
                    "resource will be stored and the streamed to the user");
            try {
                //Reflect the fact that we return a locally cached resource
                res.setResponseRepoPath(InternalRepoPathFactory.create(localCacheRepo.getKey(), path));
                ResourceStreamHandle handle = getRepositoryService().downloadAndSave(requestContext, this, res);
                return handle;
            } catch (IOException e) {
                RepoRequests.logToContext("Error occurred while downloading artifact: %s", e.getMessage());
                //If we fail on remote fetching and we can get the resource from an expired entry in
                //the local cache - fallback to using it, else rethrow the exception
                if (res.isExpired()) {
                    ResourceStreamHandle result =
                            getRepositoryService().unexpireAndRetrieveIfExists(requestContext, localCacheRepo, path);
                    if (result != null) {
                        RepoRequests.logToContext("Requested artifact is expired and exists in the cache - " +
                                "un-expiring cached and returning it instead");
                        return result;
                    }
                }
                throw e;
            }
        } else {
            RepoRequests.logToContext("Target repository is configured to not retain artifacts locally - " +
                    "resource will be stream directly to the user");
            ResourceStreamHandle handle = downloadResource(path, requestContext);
            return handle;
        }
    }

    @Override
    public ResourceStreamHandle downloadAndSave(InternalRequestContext requestContext, RepoResource remoteResource)
            throws IOException, RepoRejectException {
        assert getLocalCacheRepo() != null;

        boolean offline = isOffline();
        RepoRequests.logToContext("Remote repository is %s", (offline ? "offline" : "online"));

        RepoResource cachedResource = getLocalCacheRepo().getInfo(requestContext);
        boolean foundExpiredResourceAndNewerRemote = foundExpiredAndRemoteIsNewer(remoteResource, cachedResource);
        RepoRequests.logToContext(
                "Found expired cached resource but remote is newer = %s. Cached resource: %s, Remote resource: %s",
                foundExpiredResourceAndNewerRemote, cachedResource.getLastModified(), remoteResource.getLastModified());

        boolean forceExpiryCheck = requestContext.isForceExpiryCheck();
        RepoRequests.logToContext("Force expiration on the cached resource = %s", forceExpiryCheck);

        boolean cachedNotFoundAndNotExpired = notFoundAndNotExpired(cachedResource);
        RepoRequests.logToContext("Resource isn't cached and isn't expired = %s", cachedNotFoundAndNotExpired);

        RepoPath remoteRepoPath = remoteResource.getRepoPath();
        boolean remoteDownloadAllowed = isRemoteDownloadAllowed(remoteRepoPath);
        if (!remoteDownloadAllowed) {
            RepoRequests.logToContext("Remote resource not allowed based on one of the interceptors");
        }

        // Retrieve remote artifact conditionally
        if (!offline && remoteDownloadAllowed &&
                (forceExpiryCheck || foundExpiredResourceAndNewerRemote || cachedNotFoundAndNotExpired)) {
            // Check for security deploy rights
            RepoRequests.logToContext("Asserting valid deployment path");
            ValidDeployPathContext validDeployPathContext = new ValidDeployPathContext
                    .Builder(localCacheRepo, remoteRepoPath)
                    .contentLength(remoteResource.getInfo().getSize())
                    .forceExpiryCheck(requestContext.isForceExpiryCheck()).build();
            getRepositoryService().assertValidDeployPath(validDeployPathContext);

            // Only 1 remote downloader is allowed
            String pathToLock = cachedResource.getRepoPath().toPath();

            boolean lockAcquired = false;
            try {
                try {
                    lockAcquired = lockingMap.tryAddAndLock(pathToLock,
                            ConstantValues.repoConcurrentDownloadSyncTimeoutSecs.getLong(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.info("Interrupted on concurrent download lock of '{}'", pathToLock);
                    RepoRequests.logToContext("Interrupted on concurrent download lock of '" + pathToLock + "'");
                }
                if (lockAcquired) {
                    try {
                        if (!cachedResource.isExpired()) {
                            String message = "Found completed concurrent download - using prepared handle";
                            return getHandleFromCache(requestContext, remoteResource, cachedResource, message);
                        }
                    } catch (FileNotFoundException e) {
                        String msg = "Unable to find cached resource stream handle, continuing with actual remote download.";
                        log.debug(msg);
                        RepoRequests.logToContext(msg);
                    }
                } else {
                    //We exited because of a timeout, return timeout error
                    log.info("Timed-out waiting on concurrent download of '{}' in '{}'.", pathToLock, this);
                    RepoRequests.logToContext("Timed-out waiting on concurrent download.");
                    return null;
                }
                RepoRequests.logToContext("Found no cached resource - starting download");
                cachedResource = doDownloadAndSave(requestContext, remoteResource);
            } finally {
                if (lockAcquired) {
                    lockingMap.removeAndUnlock(pathToLock);
                }
            }
            notifyInterceptorsOnAfterRemoteDownload(remoteResource);
        }

        boolean cachedExpiredAndNewerThanRemote = cachedExpiredAndNewerThanRemote(remoteResource, cachedResource);
        RepoRequests.logToContext("Found expired cached resource and is newer than remote = %s",
                cachedExpiredAndNewerThanRemote);
        if (cachedExpiredAndNewerThanRemote) {
            synchronizeExpiredResourceProperties(remoteResource.getRepoPath());
            unexpire(cachedResource);
        }

        RepoRequests.logToContext("Returning the cached resource");
        //Return the cached result (the newly downloaded or already cached resource)
        return localCacheRepo.getResourceStreamHandle(requestContext, cachedResource);
    }

    private ResourceStreamHandle getHandleFromCache(InternalRequestContext requestContext, RepoResource remoteResource,
            RepoResource cachedResource, String message) throws IOException, RepoRejectException {
        ResourceStreamHandle cacheHandle = localCacheRepo.getResourceStreamHandle(requestContext, cachedResource);
        if (cacheHandle != null) {
            RepoRequests.logToContext(message);
            ChecksumsInfo checksumsInfo = localCacheRepo.getInfo(requestContext).getInfo().getChecksumsInfo();
            remoteResource.getInfo().getChecksumsInfo().setChecksums(checksumsInfo.getChecksums());
            return cacheHandle;
        }

        return null;
    }

    private boolean isRemoteDownloadAllowed(RepoPath remoteRepoPath) {
        for (RemoteRepoInterceptor interceptor : interceptors) {
            RemoteRepoDescriptor descriptor = getDescriptor();
            if (!interceptor.isRemoteDownloadAllowed(descriptor, remoteRepoPath)) {
                // one veto is enough to prevent the download
                return false;
            }
        }
        return true;
    }

    private boolean isRemoteRepoListingAllowed(String remoteDirectoryPath) {
        for (RemoteRepoInterceptor interceptor : interceptors) {
            RemoteRepoDescriptor descriptor = getDescriptor();
            if (!interceptor.isRemoteRepoListingAllowed(descriptor, remoteDirectoryPath)) {
                // one veto is enough to prevent the download
                return false;
            }
        }
        return true;
    }

    protected void notifyInterceptorsOnAfterRemoteDownload(RepoResource remoteResource) {
        for (RemoteRepoInterceptor interceptor : interceptors) {
            interceptor.afterRemoteDownload(remoteResource);
        }
    }

    protected void notifyInterceptorsOnBeforeRemoteHttpMethodExecution(HttpRequestBase request,
            Map<String, String> headers) {
        for (RemoteRepoInterceptor interceptor : interceptors) {
            interceptor.beforeRemoteHttpMethodExecution(request, headers);
        }
    }

    // this is the actual download of the resource
    private RepoResource doDownloadAndSave(InternalRequestContext requestContext, RepoResource remoteResource)
            throws RepoRejectException, IOException {
        RepoRequests.logToContext("Downloading and saving");
        RepoPath remoteRepoPath = remoteResource.getRepoPath();
        ResourceStreamHandle handle = null;
        try {
            beforeResourceDownload(remoteResource, requestContext.getProperties(), requestContext.getRequest());

            RepoResourceInfo remoteInfo = remoteResource.getInfo();
            Set<ChecksumInfo> remoteChecksums = remoteInfo.getChecksumsInfo().getChecksums();
            boolean receivedRemoteChecksums = CollectionUtils.notNullOrEmpty(remoteChecksums);
            if (receivedRemoteChecksums) {
                RepoRequests.logToContext("Received remote checksums headers - %s", remoteChecksums);
            } else {
                RepoRequests.logToContext("Received no remote checksums headers");
            }

            //Allow plugins to provide an alternate content
            handle = getAltContent(remoteRepoPath);

            if (handle == null && receivedRemoteChecksums &&
                    shouldSearchForExistingResource(requestContext.getRequest())) {
                RepoRequests.logToContext("Received no alternative content, received remote checksums headers" +
                        " and searching for existing resources on download is enabled");
                handle = getExistingResourceByChecksum(remoteChecksums, remoteResource.getSize());
            }

            long remoteRequestStartTime = 0;
            if (handle == null) {
                RepoRequests.logToContext("Received no alternative content or existing resource - " +
                        "downloading resource");
                //If we didn't get an alternate handle do the actual download
                remoteRequestStartTime = System.currentTimeMillis();
                handle = downloadResource(remoteRepoPath.getPath(), requestContext);
            }

            if (!receivedRemoteChecksums) {
                RepoRequests.logToContext("Trying to find remote checksums");
                remoteChecksums = getRemoteChecksums(remoteRepoPath.getPath());
                if (remoteResource instanceof RemoteRepoResource) {
                    ((RemoteRepoResource) remoteResource).getInfo().setChecksums(remoteChecksums);
                } else {
                    // Cannot set the checksums on non remote repo resource
                    RepoRequests.logToContext("No checksums found on %s and it's not a remote resource!",
                            remoteResource);
                }
            }
            boolean synchronizeProperties =isSynchronizeProperties();

            RepoRequests.logToContext("Remote property synchronization enabled = %s", synchronizeProperties);

            Properties properties = null;
            if (synchronizeProperties) {
                // No check for annotate permissions, since sync props is a configuration flag
                // and file will be deployed here
                RepoRequests.logToContext("Trying to find remote properties");
                properties = getRemoteProperties(remoteRepoPath.getPath());
            }

            //Create/override the resource in the storage cache
            RepoRequests.logToContext("Saving resource to " + localCacheRepo);
            SaveResourceContext saveResourceContext = new SaveResourceContext.Builder(remoteResource, handle)
                    .properties(properties).build();
            RepoResource cachedResource = getRepositoryService().saveResource(localCacheRepo, saveResourceContext);
            if (remoteRequestStartTime > 0) {
                String remoteAddress;
                if (handle instanceof HttpRepo.MyRemoteResourceStreamHandle) {
                    remoteAddress = ((HttpRepo.MyRemoteResourceStreamHandle) handle).getRemoteIp();
                } else {
                    remoteAddress = StringUtils.EMPTY;
                }
                // fire upload event only if the resource was downloaded from the remote repository
                UploadEntry uploadEntry = new UploadEntry(remoteResource.getRepoPath().getId(),
                        cachedResource.getSize(), System.currentTimeMillis() - remoteRequestStartTime, remoteAddress);
                TrafficService trafficService = ContextHelper.get().beanForType(TrafficService.class);
                trafficService.handleTrafficEntry(uploadEntry);
            }

            unexpire(cachedResource);
            return cachedResource;
        } catch (Exception e) {
            // set exception here before the remote stream is closed to signal an error
            Throwable ioCause = ExceptionUtils.getCauseOfTypes(e, IOException.class);
            if (ioCause != null) {
                log.error("IO error while trying to download resource '{}': {}: {}", remoteRepoPath,
                        ioCause.getClass().getName(), HttpClientUtils.getErrorMessage(ioCause));
                log.debug("IO error while trying to download resource '{}': {}",
                        remoteResource.getRepoPath(), ioCause.getMessage(), ioCause);
                setExceptionOnHandle(handle, e);
                throw (IOException) ioCause;
            }
            setExceptionOnHandle(handle, e);
            throw e;
        } finally {
            Closeables.close(handle, false);
        }
    }

    private boolean isSynchronizeProperties() {
        boolean synchronizeProperties = getDescriptor().isSynchronizeProperties();
        ContentSynchronisation contentSynchronisation = getDescriptor().getContentSynchronisation();
        if(contentSynchronisation!=null){
            synchronizeProperties = synchronizeProperties || contentSynchronisation.getProperties().isEnabled();
        }
        return synchronizeProperties;
    }

    private void setExceptionOnHandle(ResourceStreamHandle handle, Exception e) {
        if (handle instanceof RemoteResourceStreamHandle) {
            ((RemoteResourceStreamHandle) handle).setThrowable(e);
        }
    }

    private boolean shouldSearchForExistingResource(Request request) {
        String searchForExistingResource = request.getParameter(
                ArtifactoryRequest.PARAM_SEARCH_FOR_EXISTING_RESOURCE_ON_REMOTE_REQUEST);
        if (StringUtils.isNotBlank(searchForExistingResource)) {
            return Boolean.valueOf(searchForExistingResource);
        }

        return ConstantValues.searchForExistingResourceOnRemoteRequest.getBoolean();
    }

    private ResourceStreamHandle getExistingResourceByChecksum(Set<ChecksumInfo> remoteChecksums, long size) {
        String remoteSha1 = getRemoteSha1(remoteChecksums);
        if (!ChecksumType.sha1.isValid(remoteSha1)) {
            RepoRequests.logToContext("Remote sha1 doesn't exist or is invalid: " + remoteSha1);
            return null;
        }
        try {
            RepoRequests.logToContext("Searching for existing resource with SHA-1 '%s'", remoteSha1);
            BinaryStore binaryStore = ContextHelper.get().beanForType(BinaryStore.class);
            InputStream data = binaryStore.getBinary(remoteSha1);
            RepoRequests.logToContext("Found existing resource with the same checksum - " +
                    "returning as normal content handle");
            return new SimpleResourceStreamHandle(data, size);
        } catch (BinaryNotFoundException e) {
            // not found - resume
            return null;
        }
    }

    private String getRemoteSha1(Set<ChecksumInfo> remoteChecksums) {
        for (ChecksumInfo remoteChecksum : remoteChecksums) {
            if (ChecksumType.sha1.equals(remoteChecksum.getType())) {
                return remoteChecksum.getOriginal();
            }
        }
        return null;
    }

    private void unexpire(RepoResource cachedResource) {
        RepoRequests.logToContext("Un-expiring cached resource if needed");
        String relativePath = cachedResource.getRepoPath().getPath();
        boolean isMetadata = cachedResource.isMetadata();

        RepoRequests.logToContext("Is resource metadata = %s", isMetadata);

        if (!isMetadata) {
            // unexpire the file
            RepoRequests.logToContext("Un-expiring the resource");
            getRepositoryService().unexpireIfExists(localCacheRepo, relativePath);
            // remove it from bad retrieval caches
            RepoRequests.logToContext("Removing the resource from all failed caches");
            removeFromCaches(relativePath, false);
        }
    }

    private boolean foundExpiredAndRemoteIsNewer(RepoResource remoteResource, RepoResource cachedResource) {
        return cachedResource.isExpired() && remoteResource.getLastModified() > cachedResource.getLastModified();
    }

    private boolean cachedExpiredAndNewerThanRemote(RepoResource remoteResource, RepoResource cachedResource) {
        return cachedResource.isExpired() && remoteResource.getLastModified() <= cachedResource.getLastModified();
    }

    private boolean notFoundAndNotExpired(RepoResource cachedResource) {
        return !cachedResource.isFound() && !cachedResource.isExpired();
    }

    private Set<ChecksumInfo> getRemoteChecksums(String path) {
        Set<ChecksumInfo> checksums = new HashSet<>();
        for (ChecksumType checksumType : ChecksumType.BASE_CHECKSUM_TYPES) {
            String checksum = null;
            try {
                RepoRequests.logToContext("Trying to find remote checksum - %s", checksumType.ext());
                checksum = getRemoteChecksum(path + checksumType.ext());
            } catch (FileNotFoundException e) {
                RepoRequests.logToContext("Remote checksum file doesn't exist");
            } catch (Exception e) {
                RepoRequests.logToContext("Error occurred while retrieving remote checksum: %s", e.getMessage());
            }
            ChecksumInfo info = new ChecksumInfo(checksumType, null, null);
            if (StringUtils.isNotBlank(checksum)) {
                RepoRequests.logToContext("Found remote checksum with the value - %s", checksum);
                // set the remote checksum only if it is a valid string for that checksum
                if (checksumType.isValid(checksum)) {
                    info = new ChecksumInfo(checksumType, checksum, null);
                } else {
                    RepoRequests.logToContext("Remote checksum is invalid");
                }
            }
            checksums.add(info);
        }
        return checksums;
    }

    @Override
    public String getChecksum(String path, RepoResource res) throws IOException {
        String value = null;
        if (isStoreArtifactsLocally()) {
            //We assume the resource is already contained in the repo-cache
            value = localCacheRepo.getChecksum(path, res);
        } else {
            try {
                value = getRemoteChecksum(path);
            } catch (RemoteRequestException e) {
                // ok to fail with 404, just return null (which translates to not exist in higher levels)
                if (e.getRemoteReturnCode() != HttpStatus.SC_NOT_FOUND) {
                    throw e;
                }
            }
        }
        return value;
    }

    @Override
    @Nullable
    public LocalCacheRepo getLocalCacheRepo() {
        return localCacheRepo;
    }

    @Override
    @Nonnull
    public List<RemoteItem> listRemoteResources(String directoryPath) {
        assert !isOffline() : "Should never be called in offline mode";
        if (!isRemoteRepoListingAllowed(directoryPath)) {
            return Collections.emptyList();
        }
        List<RemoteItem> cachedUrls = remoteResourceCache.get(directoryPath);
        if (CollectionUtils.notNullOrEmpty(cachedUrls)) {
            return cachedUrls;
        }

        UnfoundRepoResource unfoundRepoResource = ((UnfoundRepoResource) missedRetrievalsCache.get(directoryPath));
        if (unfoundRepoResource != null) {
            return Collections.emptyList();
        }

        List<RemoteItem> urls = null;
        String fullDirectoryUrl = directoryPath;
        if (!HttpUtils.isAbsolute(directoryPath)) {
            fullDirectoryUrl = removeApiFromUrlAndAppend(directoryPath);
        }

        try {
            urls = getChildUrls(fullDirectoryUrl);
        } catch (IOException e) {
            log.debug("Error while listing remote resources", e);
            addRemoteListingEntryToMissedCache(directoryPath, e);
            RemoteRequestException remoteError =
                    (RemoteRequestException) ExceptionUtils.getCauseOfTypes(e, RemoteRequestException.class);
            if (remoteError == null || remoteError.getRemoteReturnCode() != HttpStatus.SC_NOT_FOUND) {
                log.info("Error listing remote resources {}: {}", fullDirectoryUrl, e.getMessage());
            }
        }

        if (CollectionUtils.isNullOrEmpty(urls)) {
            log.debug("No remote URLS where found for: {}", fullDirectoryUrl);
            return Lists.newArrayList();
        }
        remoteResourceCache.put(directoryPath, urls);
        return urls;
    }

    protected String appendAndGetUrl(String pathToAppend) {
        boolean isPropertiesRequest = pathToAppend.endsWith(":properties");
        if (HttpUtils.isAbsolute(pathToAppend) && !isPropertiesRequest) {
            return pathToAppend;
        }

        if (isPropertiesRequest) {
            return removeApiFromUrlAndAppend(pathToAppend);
        }

        String remoteUrl = getUrl();
        StringBuilder baseUrlBuilder = new StringBuilder(remoteUrl);
        if (!remoteUrl.endsWith("/")) {
            baseUrlBuilder.append("/");
        }
        baseUrlBuilder.append(pathToAppend);
        return baseUrlBuilder.toString();
    }

    private String removeApiFromUrlAndAppend(String pathToAppend) {
        //If remote url ends with / it messes up the path builder, since we already add it below it's safe to remove
        String remoteUrl = PathUtils.trimTrailingSlashes(getUrl());
        ArtifactoryStandardUrlResolver artifactoryStandardUrlResolver = new ArtifactoryStandardUrlResolver(remoteUrl);
        StringBuilder baseUrlBuilder = new StringBuilder(artifactoryStandardUrlResolver.getBaseUrl())
                .append("/").append(artifactoryStandardUrlResolver.getRepoKey());
        if (!remoteUrl.endsWith("/")) {
            baseUrlBuilder.append("/");
        }
        baseUrlBuilder.append(pathToAppend);
        return baseUrlBuilder.toString();
    }

    private void addRemoteListingEntryToMissedCache(String directoryPath, IOException e) {
        if (!missedRetrievalsCache.containsKey(directoryPath)) {
            String message = e.getMessage();
            missedRetrievalsCache.put(directoryPath, new UnfoundRepoResource(getRepoPath(directoryPath), message));
        }
    }

    protected abstract List<RemoteItem> getChildUrls(String dirUrl) throws IOException;

    @Override
    public void clearCaches() {
        clearCaches(missedRetrievalsCache, remoteResourceCache);
    }

    @Override
    public void removeFromCaches(String path, boolean removeSubPaths) {
        removeFromCaches(path, removeSubPaths, missedRetrievalsCache, remoteResourceCache);
    }

    /**
     * Executed before actual download. May return an alternate handle with its own input stream to circumvent download
     */
    private void beforeResourceDownload(RepoResource resource, Properties properties, Request request) {
        boolean fetchSourcesEagerly = getDescriptor().isFetchSourcesEagerly();
        boolean fetchJarsEagerly = getDescriptor().isFetchJarsEagerly();

        RepoRequests.logToContext("Eager source JAR fetching enabled = %s", fetchSourcesEagerly);
        RepoRequests.logToContext("Eager JAR fetching enabled = %s", fetchJarsEagerly);

        if (!fetchSourcesEagerly && !fetchJarsEagerly) {
            // eager fetching is disabled
            RepoRequests.logToContext("Eager JAR and source JAR fetching is disabled");
            return;
        }
        String replicationDownload = request.getParameter(ArtifactoryRequest.PARAM_REPLICATION_DOWNLOAD_REQUESET);
        if (StringUtils.isNotBlank(replicationDownload) && Boolean.valueOf(replicationDownload)) {
            // Do not perform eager fetching in case of replication download
            RepoRequests.logToContext("Eager JAR and source JAR fetching is disabled for replication download request");
            return;
        }
        RepoPath repoPath = resource.getRepoPath();
        MavenArtifactInfo artifactInfo = MavenArtifactInfo.fromRepoPath(repoPath);
        boolean validMavenArtifactInfo = artifactInfo.isValid();
        boolean artifactHasClassifier = artifactInfo.hasClassifier();

        RepoRequests.logToContext("Valid Maven artifact info = %s", validMavenArtifactInfo);
        RepoRequests.logToContext("Artifact has classifier = %s", artifactHasClassifier);

        if (!validMavenArtifactInfo || artifactHasClassifier) {
            RepoRequests.logToContext("Eager JAR and source JAR fetching is not attempted");
            return;
        }

        String eagerPath;

        boolean artifactIsPom = "pom".equals(artifactInfo.getType());
        boolean artifactIsJar = "jar".equals(artifactInfo.getType());

        MavenArtifactInfo eagerFetchArtifactInfo = new MavenArtifactInfo(artifactInfo);
        if (fetchJarsEagerly && artifactIsPom) {
            eagerFetchArtifactInfo.setType(MavenArtifactInfo.JAR);
            eagerPath = eagerFetchArtifactInfo.getPath();
            RepoRequests.logToContext("Eagerly fetching JAR '%s'", eagerPath);
        } else if (fetchSourcesEagerly && artifactIsJar) {
            eagerFetchArtifactInfo.setClassifier("sources");
            eagerPath = eagerFetchArtifactInfo.getPath();
            RepoRequests.logToContext("Eagerly fetching source JAR '%s'", eagerPath);
        } else {
            RepoRequests.logToContext("Eager JAR and source JAR fetching is not attempted");
            return;
        }

        // Attach matrix params is exist
        eagerPath += buildRequestMatrixParams(properties);

        // pass the repo path to download eagerly
        EagerResourcesDownloader resourcesDownloader =
                InternalContextHelper.get().beanForType(EagerResourcesDownloader.class);
        RepoPath eagerRepoPath = InternalRepoPathFactory.create(getDescriptor().getKey(), eagerPath);
        resourcesDownloader.downloadAsync(eagerRepoPath);
    }

    /**
     * Returns the checksum value from the given path of a remote checksum file
     *
     * @param path Path to remote checksum
     * @return Checksum value from the remote source
     * @throws IOException If remote checksum is not found or there was a problem retrieving it
     */
    private String getRemoteChecksum(String path) throws IOException {
        if (isNonMavenRepo()) {
            return null;
        }
        if (StringUtils.contains(getUrl(), "github.com")) {
            return null;
        }

        try (ResourceStreamHandle handle = downloadResource(path)) {
            InputStream is = handle.getInputStream();
            return Checksum.checksumStringFromStream(is);
        }
    }

    private boolean isNonMavenRepo() {
        return StringUtils.contains(getUrl(), "registry.npmjs.org") || StringUtils.contains(getUrl(), "docker.io");
    }

    /**
     * Returns the remote properties of the given path
     *
     * @param relPath Relative path of artifact properties to synchronize
     * @return Properties if found in remote. Empty if not
     */
    private Properties getRemoteProperties(String relPath) {
        Properties properties = (Properties) InfoFactoryHolder.get().createProperties();
        ResourceStreamHandle handle = null;
        try {
            RepoRequests.logToContext("Trying to download remote properties");
            handle = downloadResource(relPath + ":" + Properties.ROOT);
            InputStream is = handle.getInputStream();
            if (is != null) {
                RepoRequests.logToContext("Received remote property content");
                Properties remoteProperties = (Properties) InfoFactoryHolder.get().getFileSystemXStream().fromXML(is);
                for (String remotePropertyKey : remoteProperties.keySet()) {
                    Set<String> values = remoteProperties.get(remotePropertyKey);
                    RepoRequests.logToContext("Found remote property key '{}' with values '%s'", remotePropertyKey,
                            values);
                    if (!remotePropertyKey.startsWith(ReplicationAddon.PROP_REPLICATION_PREFIX)) {
                        properties.putAll(remotePropertyKey, values);
                    }
                }
            }
        } catch (Exception e) {
            properties = null;
            RepoRequests.logToContext("Error occurred while retrieving remote properties: %s", e.getMessage());
        } finally {
            IOUtils.closeQuietly(handle);
        }
        return properties;
    }

    /**
     * To be called when retrieving an artifact which was found expired and it's remote was not newer. Synchronizes the
     * properties of the remote artifact with the local cached one
     *
     * @param repoPath Repo path to synchronize
     */
    private void synchronizeExpiredResourceProperties(RepoPath repoPath) {
        if (!getDescriptor().isSynchronizeProperties()) {
            RepoRequests.logToContext("Remote property synchronization is disabled - " +
                    "expired resource property synchronization not attempted");
            return;
        }

        try {
            String artifactRelativePath = repoPath.getPath();
            String propertiesRelativePath = artifactRelativePath + ":" + Properties.ROOT;
            RepoPath propertiesRepoPath = InternalRepoPathFactory.create(repoPath.getRepoKey(), propertiesRelativePath,
                    repoPath.isFolder());
            String remotePropertiesRelativePath = getAltRemotePath(propertiesRepoPath);
            if (!propertiesRepoPath.getPath().equals(remotePropertiesRelativePath)) {
                RepoRequests.logToContext("Remote resource path was altered by the user plugins to - %s",
                        remotePropertiesRelativePath);
            }

            LocalCacheRepo cache = getLocalCacheRepo();
            RepoResource cachedPropertiesResource;
            if (cache != null) {
                cachedPropertiesResource = cache.getInfo(new NullRequestContext(propertiesRepoPath));
            } else {
                RepoRequests.logToContext("Local cache repo was not initialized");
                return;
            }

            Properties properties = (Properties) InfoFactoryHolder.get().createProperties();

            //Send HEAD
            RepoResource remoteResource = retrieveInfo(remotePropertiesRelativePath, propertiesRepoPath.isFolder(),
                    null);
            if (remoteResource.isFound()) {
                RepoRequests.logToContext("Found remote properties");
                if (cachedPropertiesResource.isFound() &&
                        (cachedPropertiesResource.getLastModified() > remoteResource.getLastModified())) {
                    RepoRequests.logToContext("Remote properties were not modified - no changes will be applied");
                    // remote properties are not newer
                    return;
                }

                ResourceStreamHandle resourceStreamHandle = downloadResource(remotePropertiesRelativePath);
                InputStream inputStream = null;
                try {
                    inputStream = resourceStreamHandle.getInputStream();
                    Properties remoteProperties = (Properties) InfoFactoryHolder.get().getFileSystemXStream().fromXML(
                            inputStream);
                    for (String remotePropertyKey : remoteProperties.keySet()) {
                        Set<String> values = remoteProperties.get(remotePropertyKey);
                        RepoRequests.logToContext("Found remote property key '{}' with values '%s'", remotePropertyKey,
                                values);
                        if (!remotePropertyKey.startsWith(ReplicationAddon.PROP_REPLICATION_PREFIX)) {
                            properties.putAll(remotePropertyKey, values);
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            } else {
                RepoRequests.logToContext("Found no remote properties");
            }

            RepoPath localCacheRepoPath = InternalRepoPathFactory.create(cache.getKey(), artifactRelativePath);
            getRepositoryService().setProperties(localCacheRepoPath, properties);
        } catch (Exception e) {
            String repoPathId = repoPath.getId();
            log.error("Unable to synchronize the properties of the item '{}' with the remote resource: {}",
                    repoPathId, e.getMessage());
            RepoRequests.logToContext("Error occurred while synchronizing the properties: %s", e.getMessage());
        }
    }

    private RepoResource getMissedResource(String path) {
        return missedRetrievalsCache.get(path);
    }

    /**
     * Allow plugins to override the path
     *
     * @param repoPath The original repo path
     * @return Alternative path from the plugin or the same if no plugin changes it
     */
    private String getAltRemotePath(RepoPath repoPath) {
        RepoRequests.logToContext("Executing any AltRemotePath user plugins that may exist");
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        PluginsAddon pluginAddon = addonsManager.addonByType(PluginsAddon.class);
        PathCtx pathCtx = new PathCtx(repoPath.getPath());
        pluginAddon.execPluginActions(AltRemotePathAction.class, pathCtx, repoPath);
        String path = pathCtx.getPath();
        return path;
    }

    /**
     * Allow plugins to override the path
     *
     * @param repoPath
     * @return
     */
    private ResourceStreamHandle getAltContent(RepoPath repoPath) {
        RepoRequests.logToContext("Executing any AltRemoteContent user plugins that may exist");
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        PluginsAddon pluginAddon = addonsManager.addonByType(PluginsAddon.class);
        ResourceStreamCtx rsCtx = new ResourceStreamCtx();
        pluginAddon.execPluginActions(AltRemoteContentAction.class, rsCtx, repoPath);
        InputStream is = rsCtx.getInputStream();
        if (is != null) {
            RepoRequests.logToContext("Received alternative content from a user plugin - " +
                    "using as a normal content handle");
            return new SimpleResourceStreamHandle(is, rsCtx.getSize());
        }

        RepoRequests.logToContext("Received no alternative content handle from a user plugin");
        return null;
    }

    private void clearCaches(Map<String, ?>... caches) {
        for (Map<String, ?> cache : caches) {
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private void removeFromCaches(String path, boolean removeSubPaths, Map<String, ?>... caches) {
        for (Map<String, ?> cache : caches) {
            if (cache != null && !cache.isEmpty()) {
                cache.remove(path);
                if (removeSubPaths) {
                    removeSubPathsFromCache(path, cache);
                }
            }
        }
    }

    private void removeSubPathsFromCache(String basePath, Map<String, ?> cache) {
        Iterator<String> cachedPaths = cache.keySet().iterator();
        while (cachedPaths.hasNext()) {
            String key = cachedPaths.next();
            if (key.startsWith(basePath)) {
                cachedPaths.remove();
            }
        }
    }

    /**
     * Constructs a matrix params string from the given properties ready to attach to an HTTP request
     *
     * @param requestProperties Properties to construct. Can be null
     * @return HTTP request ready property chain
     */
    protected String buildRequestMatrixParams(Properties requestProperties) {
        StringBuilder requestPropertyBuilder = new StringBuilder();
        if (requestProperties != null) {
            for (Map.Entry<String, String> requestPropertyEntry : requestProperties.entries()) {
                requestPropertyBuilder.append(Properties.MATRIX_PARAMS_SEP);

                String key = requestPropertyEntry.getKey();
                boolean isMandatory = false;
                if (key.endsWith(Properties.MANDATORY_SUFFIX)) {
                    key = key.substring(0, key.length() - 1);
                    isMandatory = true;
                }
                requestPropertyBuilder.append(key);
                if (isMandatory) {
                    requestPropertyBuilder.append("+");
                }
                String value = requestPropertyEntry.getValue();
                if (StringUtils.isNotBlank(value)) {
                    requestPropertyBuilder.append("=").append(value);
                }
            }
        }
        return requestPropertyBuilder.toString();
    }
}
