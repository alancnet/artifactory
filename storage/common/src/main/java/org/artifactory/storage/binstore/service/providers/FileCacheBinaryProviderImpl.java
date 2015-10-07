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

package org.artifactory.storage.binstore.service.providers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryProviderHelper;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A binary provider that manage low level checksum files on filesystem.
 *
 * @author Fred Simon
 */
@BinaryProviderClassInfo(nativeName = "cache-fs")
public class FileCacheBinaryProviderImpl extends FileBinaryProviderBase implements FileBinaryProvider {
    private static final Logger log = LoggerFactory.getLogger(FileCacheBinaryProviderImpl.class);

    private long maxTotalSize;  // in bytes
    private Semaphore cacheCleanerSemaphore;
    private AtomicLong totalSize; // in bytes
    private ConcurrentMap<String, LruEntry> lruCache;

    @Override
    public void initialize() {
        super.initialize();
        lruCache = Maps.newConcurrentMap();
        totalSize = new AtomicLong(0);
        maxTotalSize = getLongParam("maxSize", getStorageProperties().getBinaryProviderCacheMaxSize());
        cacheCleanerSemaphore = new Semaphore(1);
        syncCacheEntries();
    }

    @Override
    protected File getBaseDataDir() {
        // For cachedFS/fullDb we want the cache to be per node and not in the HA cluster
        return ArtifactoryHome.get().getDataDir();
    }

    private void syncCacheEntries() {
        Thread cacheSyncThread = new Thread(new CacheSyncRunnable(), "cachefs-sync");
        cacheSyncThread.setDaemon(true);
        cacheSyncThread.start();
    }

    @Override
    public InputStream getStream(String sha1) {
        File cachedFile = getFile(sha1);
        try {
            // Returns from cache if there
            if (cachedFile.exists()) {
                entryAccessed(cachedFile);
                return new FileInputStream(cachedFile);
            }
        } catch (FileNotFoundException e) {
            // This is an error but not blocking
            log.error(
                    "Found cached file '" + cachedFile.getAbsolutePath() + "' " +
                            "but failed to open it due to: " + e.getMessage(), e);
        }

        try {
            // Save to a temp file while the stream is being passed to the reader!
            return new SavedToFileOnReadInputStream(next().getStream(sha1), sha1);
        } catch (IOException e) {
            throw new StorageException("Could read and save stream for " + sha1, e);
        }
    }

    @Override
    @Nonnull
    public BinaryInfo addStream(InputStream in) throws IOException {
        // Save to a temp file while the stream is being passed to the next in chain!
        SavedToFileInputStream savedToFile = null;
        try {
            savedToFile = new SavedToFileOnWriteInputStream(in);
            BinaryInfo bi = next().addStream(savedToFile);
            savedToFile.close();
            if (savedToFile.somethingWrong != null) {
                throw new IOException(
                        "Something went wrong saving '" + savedToFile.getTempFile().getAbsolutePath() + "'",
                        savedToFile.somethingWrong);
            }

            File cachedFile = getFile(bi.getSha1());
            if (cachedFile.exists()) {
                if (cachedFile.length() != bi.getLength()) {
                    // previous check might be true if the file doesn't exist anymore so check again
                    if (cachedFile.exists()) {
                        log.error("Found a cached file with checksum '" + bi.getSha1() + "' " +
                                "but length is " + cachedFile.length() + " not " + bi.getLength());
                        log.error("Deleting cached file " + cachedFile.getAbsolutePath());
                        FileUtils.forceDelete(cachedFile);
                    }
                } else {
                    // All good already there, finally will delete the temp file
                    entryAccessed(cachedFile);
                    return bi;
                }
            }
            savedToFile.moveTempFileTo(cachedFile);
            return bi;
        } finally {
            if (savedToFile != null) {
                IOUtils.closeQuietly(savedToFile);
                savedToFile.verifyTempDeleted();
            }
        }
    }

    @Override
    protected boolean deleteNoChain(String sha1) {
        if (super.deleteNoChain(sha1)) {
            LruEntry lruEntry = lruCache.remove(sha1);
            if (lruEntry != null) {
                log.debug("Deleted entry {} saved {}", sha1, StorageUnit.toReadableString(lruEntry.fileSize));
                totalSize.getAndAdd(-lruEntry.fileSize);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void pruneFiles(BasicStatusHolder statusHolder, MovedCounter movedCounter, File first) {
        // For the cache just delete all non used files
        statusHolder.status("Starting deleting non used files in " + first.getAbsolutePath() + "!", log);
        File[] files = first.listFiles();
        if (files == null) {
            statusHolder.status("Nothing to do in " + first.getAbsolutePath() + ": "
                    + Files.readFailReason(first), log);
            return;
        }
        for (File file : files) {
            String sha1 = file.getName();
            if (getBinaryStore().isActivelyUsed(sha1)) {
                statusHolder.status("Skipping deletion for in-use artifact record: " + sha1, log);
            } else {
                lruCache.remove(sha1);
                long size = file.length();
                Files.removeFile(file);
                if (file.exists()) {
                    statusHolder.error("Could not delete file " + file.getAbsolutePath(), log);
                } else {
                    movedCounter.filesMoved++;
                    movedCounter.totalSize += size;
                }
            }
        }
    }

    void entryAccessed(File cachedFile) {
        LruEntry key = new LruEntry(cachedFile);
        LruEntry oldEntry = lruCache.put(key.sha1, key);
        if (oldEntry == null) { // a new entry was added
            long newTotalSize = totalSize.addAndGet(key.fileSize);
            if (newTotalSize > maxTotalSize) {
                cleanFiles();
            }
        }
    }

    void initEntryAccessed(File cachedFile, long initTime) {
        log.trace("Adding init entry '{}' with time '{}'", cachedFile, initTime);
        LruEntry key = new LruEntry(cachedFile, initTime);
        LruEntry oldEntry = lruCache.putIfAbsent(key.sha1, key);
        if (oldEntry == null) { // a new entry was added
            totalSize.addAndGet(key.fileSize);
        }
    }

    private void cleanFiles() {
        if (!cacheCleanerSemaphore.tryAcquire()) {
            return;
        }
        try {
            log.debug("Cleaning files cache entries since {} files have a total size {} which is bigger than {}",
                    lruCache.size(),
                    StorageUnit.toReadableString(totalSize.get()),
                    StorageUnit.toReadableString(maxTotalSize));

            List<LruEntry> orderedEntries = Lists.newArrayList(lruCache.values());
            // Sort ascending, means the first ones have smallest last access
            // So first are oldest entries ready for deletion
            Collections.sort(orderedEntries);
            int nbFilesRemoved = 0;
            for (LruEntry orderedEntry : orderedEntries) {
                if (log.isTraceEnabled()) {
                    log.trace("Trying to delete file " + orderedEntry.sha1 +
                            " of size " + StorageUnit.toReadableString(orderedEntry.fileSize) + " from cache.");
                }
                if (deleteNoChain(orderedEntry.sha1)) {
                    nbFilesRemoved++;
                }
                long currentSize = totalSize.get();
                if (currentSize < maxTotalSize) {
                    // We are good now
                    log.debug("Cleaned " + nbFilesRemoved + " from cache." +
                            " Current size " + StorageUnit.toReadableString(currentSize));
                    break;
                }
            }
        } finally {
            cacheCleanerSemaphore.release();
        }
    }

    static class LruEntry implements Comparable<LruEntry> {
        final String sha1;
        final long fileSize;
        final AtomicLong lastAccess;

        LruEntry(File cachedFile) {
            this.sha1 = cachedFile.getName();
            this.fileSize = cachedFile.length();
            this.lastAccess = new AtomicLong(System.nanoTime());
        }

        LruEntry(File cachedFile, long initTime) {
            this.sha1 = cachedFile.getName();
            this.fileSize = cachedFile.length();
            this.lastAccess = new AtomicLong(initTime);
        }

        @Override
        public int compareTo(LruEntry o) {
            return Long.compare(lastAccess.get(), o.lastAccess.get());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LruEntry lruEntry = (LruEntry) o;

            if (!sha1.equals(lruEntry.sha1)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return sha1.hashCode();
        }
    }

    class SavedToFileOnReadInputStream extends SavedToFileInputStream {
        final String sha1;

        SavedToFileOnReadInputStream(InputStream delegate, String sha1) throws IOException {
            super(delegate, BinaryProviderHelper.createTempBinFile(tempBinariesDir));
            this.sha1 = sha1;
        }

        @Override
        protected boolean afterClose() throws IOException {
            if (somethingWrong != null) {
                log.error("Something went wrong saving the cached file!", somethingWrong);
                return true;
            }
            if (!fullyRead) {
                log.debug("Did not fully read entry " + sha1 + ". Not using temp file.");
                return true;
            }
            File cachedFile = getFile(sha1);
            if (cachedFile.exists()) {
                if (cachedFile.length() != tempFile.length()) {
                    log.error("After read, found a cached file with checksum '" + sha1 + "' " +
                            "but length is " + cachedFile.length() + " not " + tempFile.length());
                    return true;
                } else {
                    // All good already there, finally will delete the temp file
                    entryAccessed(cachedFile);
                    return true;
                }
            }
            moveTempFileTo(cachedFile);
            return true;
        }

        @Override
        void moveTempFileTo(File cachedFile) throws IOException {
            super.moveTempFileTo(cachedFile);
            if (cachedFile.exists()) {
                entryAccessed(cachedFile);
            }
        }
    }

    class SavedToFileOnWriteInputStream extends SavedToFileInputStream {
        SavedToFileOnWriteInputStream(InputStream delegate) throws IOException {
            super(delegate, BinaryProviderHelper.createTempBinFile(tempBinariesDir));
        }

        @Override
        protected boolean afterClose() throws IOException {
            return false;
        }

        @Override
        void moveTempFileTo(File cachedFile) throws IOException {
            super.moveTempFileTo(cachedFile);
            if (cachedFile.exists()) {
                entryAccessed(cachedFile);
            }
        }
    }

    /**
     * Sync entries from the filesystem (some entries might be unknown in case of Artifactory restart).
     */
    private class CacheSyncRunnable implements Runnable {

        @Override
        public void run() {
            log.debug("Cache entries sync started");
            boolean active = true;
            while (active) {
                try {
                    addCacheEntries(System.nanoTime());
                    active = false;
                } catch (IOException e) {
                    log.warn("Cache entries sync error: '{}'", e.getMessage());
                    log.debug("Cache entries sync error: '" + e.getMessage() + "'", e);
                    active = sleepQuietPeriod();
                }
            }
        }

        private void addCacheEntries(final long started) throws IOException {
            final Path binariesDirPath = getBinariesDir().toPath();
            java.nio.file.Files.walkFileTree(binariesDirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (binariesDirPath.equals(dir)) {
                        return FileVisitResult.CONTINUE;
                    }

                    return dir.toString().contains("_pre") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        initEntryAccessed(file.toFile(), started);
                    } catch (Exception e) {
                        log.error("Unable to add cache entry '{}'", file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        private boolean sleepQuietPeriod() {
            try {
                Thread.sleep(ConstantValues.cacheFSSyncquietPeriodSecs.getLong());
            } catch (InterruptedException e) {
                log.debug("Cache entries sync interrupted");
                Thread.interrupted();
                return false;
            }
            return true;
        }
    }
}
