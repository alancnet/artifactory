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

package org.artifactory.storage;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.storage.StorageQuotaInfo;

/**
 * @author yoavl
 */
public interface StorageService {

    void compress(BasicStatusHolder statusHolder);

    boolean isDerbyUsed();

    void logStorageSizes();

    /**
     * Check that manual garbage collection can be run, and then activate the double GC asynchronously.
     *
     * @param statusHolder
     */
    void callManualGarbageCollect(BasicStatusHolder statusHolder);

    void pruneUnreferencedFileInDataStore(BasicStatusHolder statusHolder);

    void ping();

    /**
     * Creates a summary of the filestore storage used by Artifactory.
     *
     * @return a filestore usage summary
     */
    FileStoreStorageSummary getFileStoreStorageSummary();

    /**
     * Create and retrieve a storage quota info object which contains information about
     * the system storage total space, free space etc.
     *
     * @param fileContentLength The uploaded file content length to include in the quota calculation
     * @return The {@link org.artifactory.api.storage.StorageQuotaInfo} object, might return null if quota management doesn't exist inside
     * the central config or it is disabled.
     */
    StorageQuotaInfo getStorageQuotaInfo(long fileContentLength);

    /**
     * Creates a summary of artifacts storage, including number of items and size per repository.
     *
     * @return Summary of storage used by repository artifacts
     */
    StorageSummaryInfo getStorageSummaryInfo();

}
