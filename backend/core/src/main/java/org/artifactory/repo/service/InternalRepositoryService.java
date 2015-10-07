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

package org.artifactory.repo.service;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.StoringRepo;
import org.artifactory.repo.local.ValidDeployPathContext;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.Lock;
import org.artifactory.spring.ContextReadinessListener;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.storage.fs.service.ItemMetaInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A repository service to be used by the core
 * <p/>
 * User: freds Date: Jul 31, 2008 Time: 5:50:18 PM
 */
public interface InternalRepositoryService extends RepositoryService, ReloadableBean, ContextReadinessListener {

    boolean isAnonAccessEnabled();

    /**
     * @param key The repository key
     * @return Repository with the exact given key (no special meaning for remote/cache repo keys). Null if not found.
     */
    @Nullable
    Repo repositoryByKey(String key);

    @Nullable
    VirtualRepo virtualRepositoryByKey(String key);

    @Nullable
    LocalRepo localOrCachedRepositoryByKey(String key);

    @Nullable
    RealRepo localOrRemoteRepositoryByKey(String key);

    /**
     * Get the holder object that holds the actual repository as well as the path.
     *
     * @param repoPath The repo path
     * @param <R>      The type of repository.
     * @return The holder object that holds the actual repository as well as the path.
     */
    <R extends Repo> RepoRepoPath<R> getRepoRepoPath(RepoPath repoPath);

    /**
     * Get a repository that can store artifacts. Will get either a local repo (regular or cache) or a virtual repo. If
     * remote repository key is given, the cache repository is returned.
     */
    StoringRepo storingRepositoryByKey(String key);

    List<RealRepo> getLocalAndRemoteRepositories();

    List<VirtualRepo> getVirtualRepositories();

    List<LocalRepo> getLocalAndCachedRepositories();

    /**
     * Returns a local non-cache repository by key
     *
     * @param key The repository key
     * @return Local non-cahce repository or null if not found
     */
    LocalRepo localRepositoryByKey(String key);

    RemoteRepo remoteRepositoryByKey(String key);

    Repo nonCacheRepositoryByKey(String key);

    /**
     * This will verify the permission to deploy to the path
     *
     * @param validDeployPathContext Context for encapsulating all the methods parameters
     * @return A status holder with info on error
     */
    void assertValidDeployPath(ValidDeployPathContext validDeployPathContext) throws RepoRejectException;

    <T extends RemoteRepoDescriptor> ResourceStreamHandle downloadAndSave(InternalRequestContext requestContext,
            RemoteRepo<T> remoteRepo, RepoResource res) throws IOException, RepoRejectException;

    @Lock
    RepoResource unexpireIfExists(LocalRepo localCacheRepo, String path);

    @Lock
    ResourceStreamHandle unexpireAndRetrieveIfExists(InternalRequestContext requestContext, LocalRepo localCacheRepo,
            String path) throws IOException, RepoRejectException;

    ResourceStreamHandle getResourceStreamHandle(InternalRequestContext requestContext, Repo repo, RepoResource res)
            throws IOException, RepoRejectException;

    /**
     * Saves the given resource in the input repository.
     *
     * @param repo        The repo to save the resource in. Must be a storage repo (local/local cache/virtual cache)
     * @param saveContext Resource details
     * @return Repository resource with details of the saved resource
     * @throws IOException         On any IO related exception
     * @throws RepoRejectException If the target repository rejected the resource
     */
    RepoResource saveResource(StoringRepo repo, SaveResourceContext saveContext)
            throws IOException, RepoRejectException;

    /**
     * Internal, transactional method to save a resource. Don't use directly.
     *
     * @see InternalRepositoryService#saveResource(org.artifactory.repo.StoringRepo, org.artifactory.repo.SaveResourceContext)
     */
    @Lock
    RepoResource saveResourceInTransaction(StoringRepo repo, SaveResourceContext saveContext)
            throws IOException, RepoRejectException;

    @Override
    void exportTo(ExportSettings settings);

    boolean isRepoExistInCache(RepoPath repoPath);

    /**
     * Returns a local or local cache repository. Throws an exception if not found
     *
     * @param repoPath A repo path in the repository
     * @return Local/cache repository matching the repo path repo key
     * @throws IllegalArgumentException if repository not found
     */
    @Nonnull
    LocalRepo getLocalRepository(RepoPath repoPath);

    @Override
    @Lock
    void reload(CentralConfigDescriptor oldDescriptor);

    RepoPath getExplicitDescriptorPathByArtifact(RepoPath repoPath);

    VirtualRepo getGlobalVirtualRepo();

    /**
     * A convenient method for saving internally generated files to the repositories.
     *
     * @param fileRepoPath Repository path to store the input stream
     * @param is           The input stream to store. Will be closed before returning from this method
     * @see InternalRepositoryService#saveResource(org.artifactory.repo.StoringRepo, org.artifactory.repo.SaveResourceContext)
     */
    void saveFileInternal(RepoPath fileRepoPath, InputStream is) throws RepoRejectException, IOException;

    /**
     * @param repoPath The repo path of an item.
     * @return The meta info of the given repo path. Null if not found.
     */
    @Nullable
    ItemMetaInfo getItemMetaInfo(RepoPath repoPath);

    /**
     * Moves repository path (pointing to a folder) to another absolute target. The move will only move paths the user
     * has permissions to move and paths that are accepted by the target repository.
     *
     * @param from            The source path to move.
     * @param to              The target path to move to.
     * @param dryRun          If true the method will just report the expected result but will not move any file
     * @param suppressLayouts If true, path translation across different layouts should be suppressed.
     * @param failFast        If true, the operation should fail upon encountering an error.
     * @return MoveMultiStatusHolder holding the errors and warnings
     */
    @Lock
    MoveMultiStatusHolder moveWithoutMavenMetadata(RepoPath from, RepoPath to, boolean dryRun, boolean suppressLayouts,
            boolean failFast);

    /**
     * register Pooling Http Client Connection Manager in order to monitor the connection pool and clean idle or expire connection
     *
     * @param connectionManager - remote repository Pooling Http Client Connection Manager
     */
    void registerConnectionPoolMgr(PoolingHttpClientConnectionManager connectionManager);

    /**
     * Sets the client checksum on the target file. Fails if the target doesn't exist or not a file.
     *
     * @param repo               The repository holding the file
     * @param checksumType       The {@link org.artifactory.checksum.ChecksumType}
     * @param targetFileRepoPath Target file
     * @param checksum           Checksum value
     * @return Checksum info for the given type
     */
    @Lock
    ChecksumInfo setClientChecksum(LocalRepo repo, ChecksumType checksumType, RepoPath targetFileRepoPath,
            String checksum);
}