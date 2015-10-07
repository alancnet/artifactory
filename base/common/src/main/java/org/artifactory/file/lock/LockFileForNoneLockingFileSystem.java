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

package org.artifactory.file.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Allows to lock files in none locking file systems such as NFS
 * The usage of this class is recommended only in none locking file systems.
 *
 * @author Gidi Shabat
 */
public class LockFileForNoneLockingFileSystem implements LockFile {
    private static final Logger log = LoggerFactory.getLogger(LockFileForNoneLockingFileSystem.class);
    private File file;
    private boolean lock;

    public LockFileForNoneLockingFileSystem(File lockFile) {
        file = lockFile;
    }

    public LockFile tryLock() {
        // If lock is already obtained to me then return successfully
        if (isLockedByMe()) {
            log.debug("No need to obtaining lock file: {}. Lock already obtained.", file);
            return this;
        }
        // Check if the lock is not obtained by other server
        log.debug("Obtaining lock file: {}.", file);
        if (isLockFileExists()) {
            throw new RuntimeException("Another instance of Artifactory is already running. " +
                    "The lock file is locked by another process. [" + file + "]");
        }
        try {
            file.createNewFile();
        } catch (Exception e) {
            throw new RuntimeException("Could not create lock file. [" + file + "]", e);
        }
        lock = true;
        log.debug("Lock file: {} has been obtained ", file);
        return this;
    }

    private boolean isLockFileExists() {
        return file.exists();
    }

    public void release() {
        if (!isLockedByMe()) {
            throw new RuntimeException("Fail to release the lock. The lock file is locked by another process." +
                    " [" + file + "]");
        }
        try {
            log.debug("Releasing lock file: {}.", file);
            file.delete();
            log.debug("Lock file: {} has been released.", file);
        } catch (Exception e) {
            log.warn("Could not release lock. [" + file + "]", e);
        } finally {
            lock = false;
        }
    }

    public boolean isLockedByMe() {
        return lock;
    }
}
