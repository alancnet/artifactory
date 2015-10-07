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

package org.artifactory.maven.index;


import com.google.common.io.Closeables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.artifactory.common.ConstantValues;
import org.artifactory.io.NullResourceStreamHandle;
import org.artifactory.io.TempFileStreamHandle;
import org.artifactory.mime.MavenNaming;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RemoteRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.StoringRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.RemoteRequestException;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.schedule.TaskInterruptedException;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author freds
 * @author yoavl
 */
public class MavenIndexManager {
    private static final Logger log = LoggerFactory.getLogger(MavenIndexManager.class);

    private final RealRepo indexedRepo;
    private StoringRepo indexStorageRepo;
    private ResourceStreamHandle indexHandle;
    private ResourceStreamHandle propertiesHandle;
    private IndexStatus indexStatus = IndexStatus.NOT_CREATED;

    private enum IndexStatus {
        NOT_CREATED, NEEDS_SAVING, SKIP, ABORTED
    }

    public MavenIndexManager(RealRepo indexedRepo) {
        if (indexedRepo == null) {
            throw new IllegalArgumentException("Repository for indexing cannot be null.");
        }
        this.indexedRepo = indexedRepo;
        if (indexedRepo.isLocal()) {
            indexStorageRepo = (LocalRepo) indexedRepo;
        }
        indexStatus = IndexStatus.NOT_CREATED;
    }

    /**
     * Used for virtual repo merged index, where there's no need to compute the index (scan)
     */
    public MavenIndexManager(
            StoringRepo indexStorageRepo, ResourceStreamHandle indexHandle, ResourceStreamHandle propertiesHandle) {
        this.indexStorageRepo = indexStorageRepo;
        this.indexHandle = indexHandle;
        this.propertiesHandle = propertiesHandle;
        indexedRepo = null;
        indexStatus = IndexStatus.NEEDS_SAVING;
    }

    boolean fetchRemoteIndex(boolean forceRemoteDownload) {
        if (indexedRepo.isLocal()) {
            return false;
        } else {
            //For remote repositories, try to download the remote cache. If fails - index locally
            RemoteRepo remoteRepo = (RemoteRepo) indexedRepo;
            if (remoteRepo.isStoreArtifactsLocally()) {
                indexStorageRepo = remoteRepo.getLocalCacheRepo();
            }
            if (remoteRepo.isOffline()) {
                log.debug("Not retrieving index for remote repository '{}'.", indexedRepo.getKey());
                if (!isIndexFilesDontExistInCache(remoteRepo)) {
                    log.debug("Skipping indexing for remote offline repository '{}', Index exists in cache.",
                            indexedRepo.getKey());
                    indexStatus = IndexStatus.SKIP;
                }
                unExpireIndexIfExists(remoteRepo);
                return false;
            }

            File tempIndex = null;
            File tempProperties = null;
            ResourceStreamHandle remoteIndexHandle = null;
            ResourceStreamHandle remotePropertiesHandle = null;
            try {
                //Never auto-fetch the index from central if it cannot be stored locally unless force flag is enabled
                if (!forceRemoteDownload && !shouldFetchRemoteIndex(remoteRepo)) {
                    //Return true so that we don't attempt to index locally as a fallback
                    return true;
                }

                //If we receive a non-modified response (with a null handle) - don't re-download the index
                log.debug("Fetching remote index files for {}", indexedRepo);
                FileOutputStream fos = null;
                try {
                    remoteIndexHandle = remoteRepo.conditionalRetrieveResource(MavenNaming.NEXUS_INDEX_GZ_PATH,
                            forceRemoteDownload);
                    if (remoteIndexHandle instanceof NullResourceStreamHandle) {
                        log.debug("No need to fetch unmodified index for remote repository '{}'.",
                                indexedRepo.getKey());
                        indexStatus = IndexStatus.SKIP;
                        return true;
                    }
                    //Save into temp files
                    tempIndex = File.createTempFile(MavenNaming.NEXUS_INDEX_GZ, null);
                    fos = new FileOutputStream(tempIndex);
                    TaskUtils.copyLarge(remoteIndexHandle.getInputStream(), fos);
                } finally {
                    IOUtils.closeQuietly(fos);
                    /**
                     * Close the handle directly after reading stream and before we start to download the properties
                     * in case the target repo does not allow multiple simultaneous connections
                     */
                    Closeables.close(remoteIndexHandle, false);
                }

                fos = null;
                try {
                    remotePropertiesHandle = remoteRepo.downloadResource(MavenNaming.NEXUS_INDEX_PROPERTIES_PATH);
                    tempProperties = File.createTempFile(MavenNaming.NEXUS_INDEX_PROPERTIES, null);
                    fos = new FileOutputStream(tempProperties);
                    TaskUtils.copyLarge(remotePropertiesHandle.getInputStream(), fos);
                } finally {
                    IOUtils.closeQuietly(fos);
                    Closeables.close(remotePropertiesHandle, false);
                }

                //Return the handle to the zip file (will be removed when the handle is closed)
                indexHandle = new TempFileStreamHandle(tempIndex);
                propertiesHandle = new TempFileStreamHandle(tempProperties);
                indexStatus = IndexStatus.NEEDS_SAVING;
                log.debug("Fetched remote index files for {}", indexedRepo);
                return true;
            } catch (IOException e) {
                closeHandles();
                FileUtils.deleteQuietly(tempIndex);
                FileUtils.deleteQuietly(tempProperties);
                log.warn("Could not retrieve remote maven index '" + MavenNaming.NEXUS_INDEX_GZ +
                        "' for repo '" + indexedRepo + "': " + e.getMessage());
                abort();
                if (isNotFoundInRemoteRepo(e) || isIndexFilesDontExistInCache(remoteRepo)) {
                    indexStatus = IndexStatus.NOT_CREATED;
                }
                unExpireIndexIfExists(remoteRepo);
                return false;
            }
        }
    }

    private void unExpireIndexIfExists(RemoteRepo remoteRepo) {
        if (!isIndexFilesDontExistInCache(remoteRepo)) {
            InternalRepositoryService repoService = InternalContextHelper.get().beanForType(
                    InternalRepositoryService.class);
            repoService.unexpireIfExists(remoteRepo.getLocalCacheRepo(), MavenNaming.NEXUS_INDEX_GZ_PATH);
            repoService.unexpireIfExists(remoteRepo.getLocalCacheRepo(), MavenNaming.NEXUS_INDEX_PROPERTIES_PATH);
        }
    }

    private boolean isNotFoundInRemoteRepo(IOException e) {
        Throwable remoteRequestException = ExceptionUtils.getCauseOfTypes(e, RemoteRequestException.class);
        return remoteRequestException != null
                && HttpStatus.SC_NOT_FOUND == ((RemoteRequestException) e).getRemoteReturnCode();
    }

    private boolean isIndexFilesDontExistInCache(RemoteRepo remoteRepo) {
        LocalCacheRepo localCacheRepo = remoteRepo.getLocalCacheRepo();
        if (localCacheRepo == null) {
            return true;
        }
        boolean indexGzDoesntExist = !localCacheRepo.itemExists(MavenNaming.NEXUS_INDEX_GZ_PATH);
        boolean indexPropertiesDontExist = !localCacheRepo.itemExists(MavenNaming.NEXUS_INDEX_PROPERTIES_PATH);
        return indexGzDoesntExist || indexPropertiesDontExist;
    }

    private void abort() {
        indexHandle = null;
        propertiesHandle = null;
        indexStatus = IndexStatus.ABORTED;
    }

    void createLocalIndex(Date fireTime, boolean remoteIndexExists) {
        if (indexStatus != IndexStatus.NOT_CREATED) {
            return;
        }
        //For remote repositories, only index locally if not already fetched remotely before and if has local
        //storage
        if (!indexedRepo.isLocal() && remoteIndexExists) {
            return;
        }

        if (!indexedRepo.isLocal() && !((RemoteRepo) indexedRepo).isStoreArtifactsLocally()) {
            log.debug("Skipping local index creation for remote repo '{}': repo does not store artifacts locally",
                    indexedRepo.getKey());
            return;
        }

        log.debug("Creating index files for {}", indexedRepo);
        RepoIndexer repoIndexer = new RepoIndexer(indexStorageRepo);
        try {
            Pair<TempFileStreamHandle, TempFileStreamHandle> tempFileStreamHandlesPair = repoIndexer.index(fireTime);
            indexHandle = tempFileStreamHandlesPair.getFirst();
            propertiesHandle = tempFileStreamHandlesPair.getSecond();
            indexStatus = IndexStatus.NEEDS_SAVING;
            log.debug("Created index files for {}", indexedRepo);
        } catch (Exception e) {
            closeHandles();
            abort();
            String message = "Failed to index repository '" + indexedRepo + "': " + e.getMessage();
            if (e instanceof TaskInterruptedException) {
                throw new TaskInterruptedException(message, e);
            }
            throw new RuntimeException(message, e);
        }
    }

    boolean saveIndexFiles() {
        log.debug("Saving index file for {}", indexStorageRepo);
        try {
            //indexStorageRepo might be a virtual repo
            if (indexedRepo != null && !indexedRepo.isLocal()) {
                if (!((RemoteRepo) indexedRepo).isStoreArtifactsLocally()) {
                    log.debug("Skipping index saving for remote repo '{}': repo does not store artifacts locally",
                            indexedRepo.getKey());
                    return false;
                }
            }

            if (indexStatus != IndexStatus.NEEDS_SAVING) {
                return false;
            }

            InternalRepositoryService repoService = InternalContextHelper.get().beanForType(
                    InternalRepositoryService.class);
            RepoPath indexFolderRepoPath = indexStorageRepo.getRepoPath(MavenNaming.NEXUS_INDEX_DIR);

            // save the index gz file
            RepoPath indexGzRepoPath = new RepoPathImpl(indexFolderRepoPath, MavenNaming.NEXUS_INDEX_GZ);
            InputStream indexInputStream = indexHandle.getInputStream();
            repoService.saveFileInternal(indexGzRepoPath, indexInputStream);

            // save the index properties file
            RepoPath indexPropsRepoPath = new RepoPathImpl(indexFolderRepoPath, MavenNaming.NEXUS_INDEX_PROPERTIES);
            InputStream propertiesInputStream = propertiesHandle.getInputStream();
            repoService.saveFileInternal(indexPropsRepoPath, propertiesInputStream);

            log.info("Successfully saved index file '{}' and index info '{}'.",
                    indexGzRepoPath, indexPropsRepoPath);
            log.debug("Saved index file for {}", indexStorageRepo);
            return true;
        } catch (Exception e) {
            closeHandles();
            abort();
            throw new RuntimeException("Failed to save index file for repo '" + indexStorageRepo + "'.", e);
        } finally {
            closeHandles();
        }
    }

    private void closeHandles() {
        IOUtils.closeQuietly(indexHandle);
        IOUtils.closeQuietly(propertiesHandle);
    }

    private boolean shouldFetchRemoteIndex(RemoteRepo remoteRepo) {
        if (!remoteRepo.isStoreArtifactsLocally() &&
                remoteRepo.getUrl().contains(ConstantValues.mvnCentralHostPattern.getString())) {
            log.debug("Central index cannot be periodically fetched.Remote repository '{}' does not support " +
                    "local index storage.", remoteRepo.getUrl());
            return false;
        }
        return true;
    }
}
