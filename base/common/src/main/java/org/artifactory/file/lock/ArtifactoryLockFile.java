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

package org.artifactory.file.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Assures this is the one and only instance of Artifactory running over the {@code artifactoryHome}
 *
 * @author mamo
 */
public class ArtifactoryLockFile implements LockFile {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryLockFile.class);

    private File file;
    private RandomAccessFile randomAccessFile;
    private FileLock fileLock;

    public ArtifactoryLockFile(File lockFile) {
        file = lockFile;
    }

    public LockFile tryLock() {
        boolean existing = file.exists();

        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            fileLock = randomAccessFile.getChannel().tryLock();
        } catch (Exception e) {
            closeRandomAccessFile();
            throw new RuntimeException("Could not create lock file. [" + file + "]", e);
        }

        if (fileLock == null) {
            throw new RuntimeException("Another instance of Artifactory is already running. " +
                    "The lock file is locked by another process. [" + file + "]");
        }

        if (existing) {
            log.warn("Found existing lock file. Artifactory was not shutdown properly. [" + file + "]");
        }

        return this;
    }

    public void release() {
        if (fileLock != null) {
            try {
                FileChannel channel = fileLock.channel();
                fileLock.release();
                channel.close();
            } catch (IOException e) {
                log.warn("Could not release lock. [" + file + "]", e);
            }
            closeRandomAccessFile();
        }

        if (!file.delete()) {
            log.warn("Could not delete lock file. [" + file + "]");
        }
        fileLock = null;
        file = null;
    }

    public boolean isLockedByMe() {
        return fileLock != null;
    }

    private void closeRandomAccessFile() {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                log.warn("Could not close lock file. [" + file + "]", e.getMessage());
            }
            randomAccessFile = null;
        }
    }
}
