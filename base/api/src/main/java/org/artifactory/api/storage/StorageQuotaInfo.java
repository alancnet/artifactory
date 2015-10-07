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

package org.artifactory.api.storage;

import java.io.File;

/**
 * Holds the storage quota information.
 *
 * @author Shay Yaakov
 */
public class StorageQuotaInfo {
    private long freeSpace;
    private long totalSpace;
    private long usedSpace;
    private double dataUsagePercentage;
    private final int diskSpaceLimitPercentage;
    private final int diskSpaceWarningPercentage;

    public StorageQuotaInfo(File dataDir, int diskSpaceLimitPercentage, int diskSpaceWarningPercentage,
            long fileContentLength) {
        this.diskSpaceLimitPercentage = diskSpaceLimitPercentage;
        this.diskSpaceWarningPercentage = diskSpaceWarningPercentage;
        build(dataDir, fileContentLength);
    }

    private void build(File dataDir, long fileContentLength) {
        freeSpace = dataDir.getFreeSpace();
        totalSpace = dataDir.getTotalSpace();
        if (fileContentLength > 0) {
            usedSpace = totalSpace + fileContentLength - freeSpace;
        } else {
            usedSpace = totalSpace - freeSpace;
        }
        dataUsagePercentage = (double) usedSpace / totalSpace * 100;
    }

    public boolean isLimitReached() {
        return dataUsagePercentage >= diskSpaceLimitPercentage;
    }

    public boolean isWarningLimitReached() {
        return dataUsagePercentage >= diskSpaceWarningPercentage;
    }

    public int getDiskSpaceLimitPercentage() {
        return diskSpaceLimitPercentage;
    }

    public int getDataUsagePercentage() {
        return (int) dataUsagePercentage;
    }

    public int getDiskSpaceWarningPercentage() {
        return diskSpaceWarningPercentage;
    }

    public String getReadableFreeSpace() {
        return StorageUnit.toReadableString(freeSpace);
    }

    public String getReadableTotalSpace() {
        return StorageUnit.toReadableString(totalSpace);
    }

    public String getReadableUsedSpace() {
        return StorageUnit.toReadableString(usedSpace);
    }

    public String getErrorMessage() {
        return String.format("Datastore disk space is too high: " +
                "Max limit: %s%%, Used: %s%%, Total: %s, Used: %s, Available: %s",
                getDiskSpaceLimitPercentage(), getDataUsagePercentage(),
                getReadableTotalSpace(), getReadableUsedSpace(), getReadableFreeSpace());
    }

    public String getWarningMessage() {
        return String.format("Datastore disk is too high: " +
                "Warning limit: %s%%, Used: %s%%, Total: %s, Used: %s, Available: %s",
                getDiskSpaceWarningPercentage(), getDataUsagePercentage(),
                getReadableTotalSpace(), getReadableUsedSpace(), getReadableFreeSpace());
    }
}
