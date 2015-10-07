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

package org.artifactory.addon;

import org.artifactory.api.repo.Async;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.common.Lock;

import javax.annotation.Nonnull;

/**
 * Core NuGet functionality interface
 *
 * @author Noam Y. Tenne
 */
public interface NuGetAddon extends Addon {

    String API_KEY_HEADER = "X-NuGet-ApiKey";
    String NUGET_USER_AGENT = "nuget";
    String API_KEY_SEPARATOR = ":";
    String REPO_KEY_PARAM = "repoKey";
    String PATH_PARAM = "path";
    String PACKAGE_ID_PARAM = "packageId";
    String PACKAGE_VERSION_PARAM = "packageVersion";
    String PARTIAL_ID_PARAM = "partialId";
    String INCLUDE_PRERELEASE_PARAM = "includePrerelease";

    /**
     * Asynchronously extracts the spec from the nupkg and saves it as a binary property (along with ID, version and digest)
     * for easier availability when searching
     *
     * @param fileInfo     NuGet package
     * @param statusHolder Status holder for logging
     * @param addToCache   Whether to add the package into the memory cache
     */
    @Async(transactional = true, delayUntilAfterCommit = true, authenticateAsSystem = true)
    void extractNuPkgInfo(FileInfo fileInfo, MutableStatusHolder statusHolder, boolean addToCache);

    /**
     * Synchronously extracts the spec from the nupkg and saves it as a binary property (along with ID, version and digest)
     * for easier availability when searching. Used mainly in batch extractions of multiple packages.
     *
     * <p><b>NOTE!</b> This method does NOT add the package into the memory cache, it is assumed
     * that the cache is getting populated in one go after extracting all of the repository packages.
     *
     * @param fileInfo     NuGet package
     * @param statusHolder Status holder for logging
     */
    @Lock
    void extractNuPkgInfoSynchronously(FileInfo file, MutableStatusHolder statusHolder);

    /**
     * Adds a NuGet package to the NuGet packages memory cache (for local repositories only!).
     * <p><b>NOTE!</b> This method assumes the repository itself is already in the cache, see {@link #afterRepoInit(String)}
     *
     * @param repoPath   The repo path of the NuGet package
     * @param properties The properties on the repo path, those with "nuget." prefix will get extracted
     */
    void addNuPkgToRepoCache(RepoPath repoPath, Properties properties);

    @Async(delayUntilAfterCommit = true)
    void addNuPkgToRepoCacheAsync(RepoPath repoPath, Properties properties);

    /**
     * Removes a NuGet package from the local NuGet packages memory cache
     *
     * @param repoKey        The repository key
     * @param packageId      The NuGet package id
     * @param packageVersion The NuGet package version
     */
    void removeNuPkgFromRepoCache(String repoKey, String packageId, String packageVersion);

    /**
     * Instantiate the remote repository instance
     *
     * @param repoService    Repo service
     * @param repoDescriptor Descriptor of repository to init
     * @param offlineMode    True if Artifactory is in offline mode
     * @param oldRemoteRepo  Old remote repo descriptor if the current one is an update
     * @return Initialized remote repository
     */
    @Nonnull
    RemoteRepo createRemoteRepo(InternalRepositoryService repoService, RemoteRepoDescriptor repoDescriptor,
            boolean offlineMode, RemoteRepo oldRemoteRepo);

    /**
     * Asynchronously adds a local NuGet repository into the NuGet packages memory cache if it's not there already
     *
     * @param repoKey The repository key to insert into the NuGet packages memory cache
     */
    @Async(authenticateAsSystem = true)
    void afterRepoInit(String repoKey);

    /**
     * Activate the NuGet packages indexing (re-extract packages nuspec and set as properties)
     * for the given local/cache/virtual repository. Each reindexing will be queued and processed asynchronously.
     *
     * @param repoKey The local/cache/virtual repository to activate NuGet packages reindex on
     */
    void requestAsyncReindexNuPkgs(String repoKey);

    //todo consider moving into another common interface of both ha-addon and nuget-addon
    void internalAddNuPkgToRepoCache(RepoPath repoPath, Properties properties);

    void internalRemoveNuPkgFromRepoCache(String repoKey, String packageId, String packageVersion);
}
