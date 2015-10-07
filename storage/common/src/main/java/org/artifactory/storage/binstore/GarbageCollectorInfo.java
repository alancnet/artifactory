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

package org.artifactory.storage.binstore;

import org.artifactory.api.storage.StorageUnit;
import org.artifactory.util.TimeUnitFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.artifactory.util.NumberFormatter.formatLong;

/**
 * Holds the garbage collection information
 *
 * @author Noam Y. Tenne
 */
public class GarbageCollectorInfo {
    private static final Logger log = LoggerFactory.getLogger(GarbageCollectorInfo.class);

    private final long gcStartTime;
    public long gcEndTime;
    public long stopScanTimestamp;
    public int candidatesForDeletion;
    public long initialSize;
    public long initialCount;
    public int checksumsCleaned;// checksum entries cleaned from the binaries table
    public int binariesCleaned; // either files from the filestore or blobs from the database (usually same as checksumsCleaned)
    public long totalSizeCleaned;
    public int archivePathsCleaned; // the amount of unique archive paths cleaned
    public int archiveNamesCleaned; // the amount of unique archive names cleaned

    public GarbageCollectorInfo() {
        gcStartTime = System.currentTimeMillis();
    }

    /**
     * Prints a summary of the collected info to the log
     *
     * @param dataStoreSize The measured size of the datastore
     */
    public void printCollectionInfo(long dataStoreSize) {
        String duration = TimeUnitFormat.getTimeString((gcEndTime - gcStartTime), TimeUnit.MILLISECONDS);
        StringBuilder msg = new StringBuilder("Storage garbage collector report:\n").append(
                "Number of binaries:      ").append(formatLong(initialCount)).append("\n").append(
                "Total execution time:    ").append(duration).append("\n").append(
                "Candidates for deletion: ").append(formatLong(candidatesForDeletion)).append("\n").append(
                "Checksums deleted:       ").append(formatLong(checksumsCleaned)).append("\n").append(
                "Binaries deleted:        ").append(formatLong(binariesCleaned)).append("\n").append(
                "Total size freed:        ").append(StorageUnit.toReadableString(totalSizeCleaned));

        if (log.isDebugEnabled()) {
            msg.append("\n").append("Unique paths deleted:    ").append(formatLong(archivePathsCleaned));
            msg.append("\n").append("Unique names deleted:    ").append(formatLong(archiveNamesCleaned));
        }

        if (dataStoreSize >= 0) {
            msg.append("\n").append("Current total size:      ").append(StorageUnit.toReadableString(dataStoreSize));
        }

        log.info(msg.toString());
    }
}