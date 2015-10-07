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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A binary provider that manage low level checksum files on filesystem.
 *
 * @author Fred Simon
 */
@BinaryProviderClassInfo(nativeName = "dynamic")
public class DynamicFileBinaryProviderImpl extends FileBinaryProviderBase implements FileBinaryProvider {
    private static final Logger log = LoggerFactory.getLogger(DynamicFileBinaryProviderImpl.class);

    private long checkPeriod;
    private AtomicLong lastCheck = new AtomicLong(0);
    private boolean active = false;

    @Override
    public void initialize() {
        super.initialize();
        this.checkPeriod = getLongParam("period", getStorageProperties().getLongProperty(
                StorageProperties.Key.binaryProviderFilesystemSecondCheckPeriod.key(), 30000L));
    }

    @Override
    public boolean isAccessible() {
        verifyState(binariesDir);
        return isActive();
    }

    @Override
    protected void verifyState(File binariesDir) {
        try {
            super.verifyState(binariesDir);
            if (!active) {
                active = true;
                log.info("Binary store " + binariesDir.getAbsolutePath() + " activated!");
            }
        } catch (Exception e) {
            markInactive(binariesDir, e);
        }
    }

    public void markInactive(Exception e) {
        if (active) {
            markInactive(binariesDir, e);
        }
    }

    private void markInactive(File binariesDir, Exception e) {
        active = false;
        String msg = "Binary store " + binariesDir.getAbsolutePath() + " deactivated due to: " + e.getMessage();
        if (log.isDebugEnabled()) {
            log.error(msg, e);
        } else {
            log.error(msg);
        }
    }

    @Override
    @Nonnull
    public BinaryInfo addStream(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "Adding a file to dynamic should be called from Multiple File Provider!");
    }

    @Override
    protected void pruneFiles(BasicStatusHolder statusHolder, MovedCounter movedCounter, File first) {
        statusHolder.status("Starting checking if files in " + first.getAbsolutePath() + " are in DB!", log);
        //Set<DataIdentifier> identifiersSet = getIdentifiersSet();
        File[] files = first.listFiles();
        if (files == null) {
            statusHolder.status("Nothing to do in " + first.getAbsolutePath() + " " + Files.readFailReason(first), log);
            return;
        }
        Set<String> filesInFolder = new HashSet<>(files.length);
        for (File file : files) {
            filesInFolder.add(file.getName());
        }
        Set<String> existingSha1 = getBinaryStore().isInStore(filesInFolder);
        for (File file : files) {
            String sha1 = file.getName();
            if (!existingSha1.contains(sha1)) {
                if (getBinaryStore().isActivelyUsed(sha1)) {
                    statusHolder.status("Skipping deletion for in-use artifact record: " + sha1, log);
                } else {
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
    }

    public boolean isActive() {
        long l = lastCheck.longValue();
        if (System.currentTimeMillis() - l > checkPeriod) {
            if (lastCheck.compareAndSet(l, System.currentTimeMillis())) {
                verifyState(binariesDir);
            }
        }
        return active;
    }
}
