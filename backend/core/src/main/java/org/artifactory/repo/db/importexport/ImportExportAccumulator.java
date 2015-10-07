/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.repo.db.importexport;

import org.artifactory.util.TimeUnitFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Progress notifier, logs out import export progress.
 *
 * @author Gidi Shabat
 */
public class ImportExportAccumulator {
    private static final Logger log = LoggerFactory.getLogger(ImportExportAccumulator.class);
    private final int emitMessageValueEvery;

    public static enum ProgressAccumulatorType {
        IMPORT, EXPORT
    }

    private final NumberFormat numberFormat = new DecimalFormat("###.##");
    private final String repositoryKey;
    private final ProgressAccumulatorType type;
    private final long startTime;
    private long endTime;

    private int skippedFilesCount = 0;
    private int skippedFoldersCount = 0;
    private int successfulFilesCount = 0;
    private int successfulFoldersCount = 0;

    public ImportExportAccumulator(String repositoryKey, @Nonnull ProgressAccumulatorType type) {
        this.emitMessageValueEvery = ProgressAccumulatorType.EXPORT.equals(type) ? 3000 : 1000;
        this.repositoryKey = repositoryKey;
        this.type = type;
        this.startTime = System.nanoTime();
    }

    public void accumulateSuccessfulFile() {
        successfulFilesCount++;
        printProgress();
    }

    public void accumulateSkippedFile() {
        skippedFilesCount++;
        printProgress();
    }


    public void accumulateSuccessfulFolder() {
        successfulFoldersCount++;
        printProgress();
    }

    public void accumulateSkippedFolder() {
        skippedFoldersCount++;
        printProgress();
    }

    public void finished() {
        this.endTime = System.nanoTime();
    }

    private void printProgress() {
        int totalSuccessfulItemsCount = successfulFilesCount + successfulFoldersCount;
        int totalSkippedItemsCount = skippedFilesCount + skippedFoldersCount;
        if (totalSuccessfulItemsCount % emitMessageValueEvery == 0) {
            boolean exportProcess = ProgressAccumulatorType.EXPORT.equals(type);
            if (exportProcess) {
                log.info("{} exported {} items ({} files {} folders {} ips) " +
                        "{} skipped items ({} files {} folders)...", repositoryKey, totalSuccessfulItemsCount,
                        successfulFilesCount, successfulFoldersCount, getItemsPerSecond(), totalSkippedItemsCount,
                        skippedFilesCount, skippedFoldersCount);
            } else {
                log.info("{} imported {} items ({} files {} folders {} ips) " +
                        "{} skipped items ({} files {} folders)...", repositoryKey, totalSuccessfulItemsCount,
                        successfulFilesCount, successfulFoldersCount, getItemsPerSecond(), totalSkippedItemsCount,
                        skippedFilesCount, skippedFoldersCount);
            }
        }
    }

    public int getSuccessfulItemsCount() {
        return getSuccessfulFilesCount() + getSuccessfulFoldersCount();
    }

    public int getSkippedItemsCount() {
        return getSkippedFilesCount() + getSkippedFoldersCount();
    }

    public int getSkippedFilesCount() {
        return skippedFilesCount;
    }

    public int getSkippedFoldersCount() {
        return skippedFoldersCount;
    }

    public int getSuccessfulFilesCount() {
        return successfulFilesCount;
    }

    public int getSuccessfulFoldersCount() {
        return successfulFoldersCount;
    }

    public String getDurationString() {
        return TimeUnitFormat.getTimeString(getDurationNanos());
    }

    /**
     * @return A string with the number of items imported per second
     */
    public String getItemsPerSecond() {
        long duration = getDurationNanos();
        double durationSecs = duration / 1_000_000_000.0;
        double itemsPerSecond = (successfulFilesCount + successfulFoldersCount) / durationSecs;
        return numberFormat.format(itemsPerSecond);
    }

    private long getDurationNanos() {
        return endTime > 0 ? endTime - startTime : System.nanoTime() - startTime;
    }
}
