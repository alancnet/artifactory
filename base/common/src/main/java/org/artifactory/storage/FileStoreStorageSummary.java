/*
* Artifactory is a binaries repository manager.
* Copyright (C) 2014 JFrog Ltd.
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;

import static org.artifactory.storage.StorageProperties.BinaryProviderType;
import static org.artifactory.storage.StorageProperties.BinaryProviderType.cachedFS;
import static org.artifactory.storage.StorageProperties.BinaryProviderType.fullDb;

/**
 * Summary of the storage used by Artifactory on the filesystem.
 * This is usually either the binaries filestore or the cache folder.
 *
 * @author Yossi Shaul
 */
public class FileStoreStorageSummary implements Serializable {
    private final File binariesFolder;
    private final BinaryProviderType binariesStorageType;
    private long freeSpace;
    private long totalSpace;
    private long usedSpace;
    private long cacheSize;

    public FileStoreStorageSummary(@Nullable File binariesFolder, BinaryProviderType binariesStorageType) {
        this.binariesFolder = binariesFolder;
        this.binariesStorageType = binariesStorageType;
        freeSpace = binariesFolder != null ? binariesFolder.getFreeSpace() : 0;
        totalSpace = binariesFolder != null ? binariesFolder.getTotalSpace() : 0;
        usedSpace = totalSpace - freeSpace;
    }

    public FileStoreStorageSummary(File binariesFolder, StorageProperties storageProperties) {
        this(binariesFolder, storageProperties.getBinariesStorageType());
        if (fullDb.equals(binariesStorageType) || cachedFS.equals(binariesStorageType)) {
            cacheSize = storageProperties.getBinaryProviderCacheMaxSize();
        } else {
            cacheSize = -1L;
        }
    }

    /**
     * @return The type of the binaries storage (filesystem, full db etc.)
     */
    public BinaryProviderType getBinariesStorageType() {
        return binariesStorageType;
    }

    /**
     * @return The location on the filesystem storing the binaries (either the filestore or the cache). Might be null
     * when configured to use full db without a cache
     */
    @Nullable
    public File getBinariesFolder() {
        return binariesFolder;
    }

    /**
     * @return The total space in bytes on the device containing {@link FileStoreStorageSummary#binariesFolder}
     */
    public long getTotalSpace() {
        return totalSpace;
    }

    /**
     * @return The free space, in bytes, on the device containing {@link FileStoreStorageSummary#binariesFolder}
     */
    public long getUsedSpace() {
        return usedSpace;
    }

    /**
     * @return The free space, in bytes, on the device containing {@link FileStoreStorageSummary#binariesFolder}
     */
    public long getFreeSpace() {
        return freeSpace;
    }

    /**
     * @return Used space fraction
     */
    public double getUsedSpaceFraction() {
        return (double) usedSpace / totalSpace;
    }

    /**
     * @return Free space fraction
     */
    public double getFreeSpaceFraction() {
        return (double) freeSpace / totalSpace;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    protected void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

}
