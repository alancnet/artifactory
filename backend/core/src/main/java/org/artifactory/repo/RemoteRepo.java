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

import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.remote.browse.RemoteItem;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.request.RequestContext;
import org.artifactory.resource.ResourceStreamHandle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public interface RemoteRepo<T extends RemoteRepoDescriptor> extends RealRepo<T> {
    long getRetrievalCachePeriodSecs();

    boolean isStoreArtifactsLocally();

    String getUrl();

    @Nullable
    LocalCacheRepo getLocalCacheRepo();

    /**
     * Downloads a resource from the remote repository
     *
     * @return A handle for the remote resource
     */
    ResourceStreamHandle downloadResource(String relPath) throws IOException;

    ResourceStreamHandle downloadResource(String relPath, RequestContext requestContext) throws IOException;

    /**
     * Retrieves a resource remotely if the remote resource was found and is newer or if forced
     */
    ResourceStreamHandle conditionalRetrieveResource(String relPath, boolean forceRemoteDownload) throws IOException;

    void clearCaches();

    /**
     * Removes a path from the repository caches (missed and failed)
     *
     * @param path           The path to remove from the cache. The path is relative path from the repository root.
     * @param removeSubPaths If true will also remove any sub paths from the caches.
     */
    void removeFromCaches(String path, boolean removeSubPaths);

    boolean isOffline();

    /**
     * Performs the actual remote download of the artifact.
     * This method might creates new nodes and hence must be called within a transaction.
     *
     * @param requestContext The download request context
     * @param remoteResource A remote resource that has been returned by getInfo()
     * @return
     * @throws IOException         On remote download failure
     * @throws RepoRejectException If the current repo is configured not to accept the artifact
     */
    ResourceStreamHandle downloadAndSave(InternalRequestContext requestContext, RepoResource remoteResource)
            throws IOException, RepoRejectException;

    /**
     * List remote resources from a remote path.
     *
     * @param directoryPath The path of the remote repository listing
     * @return A list of URLs that represent the remote hrefs of the remote resources. Empty if not found of failed to parse the response.
     */
    @Nonnull
    List<RemoteItem> listRemoteResources(String directoryPath);

    /**
     * @return True if this repo supports listing remote directories AND it's not offline AND it's not blacklisted.
     */
    boolean isListRemoteFolderItems();

    /**
     * @return True if this repository is assumed to be offline due to download requests exceptions
     */
    boolean isAssumedOffline();

    /**
     * @return The next date (in milliseconds) the online monitor will check for online status of an assumed offline
     * repository. 0 if the repository is not assumed offline.
     * @see RemoteRepo#isAssumedOffline()
     */
    long getNextOnlineCheckMillis();

    /**
     * Manually reset the assumed offline flag to false (i.e., the repository is considered back online)
     */
    void resetAssumedOffline();

    /**
     * Cleanup any resources/threads this repository holds in order to be eligible for garbage collection
     */
    void cleanupResources();
}