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

package org.artifactory.storage.fs.lock;

import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Yossi Shaul
 */
public class LockingHelper {

    @Nonnull
    private static StorageSession getSession() {
        StorageSession session = StorageSessionHolder.getSession();
        if (session == null) {
            throw new IllegalStateException("Session doesn't exist. Please add the " +
                    Lock.class.getName() + " annotation to your method.");
        }
        return session;
    }

    public static FsItemLockEntry writeLock(LockEntryId lockEntryId) {
        //log.trace("Acquiring write lock on {} in lm={}", lockEntryId, this.hashCode());
        return getSession().writeLock(lockEntryId);
    }

    /**
     * Removes the lock entry the current thread is holding on the repo path (doesn't care if it's read or write lock).
     *
     * @param repoPath The repository path to release the lock from
     */
    public static void removeLockEntry(RepoPath repoPath) {
        getSession().removeLockEntry(repoPath);
    }

    @Nullable
    public static FsItemLockEntry getLockEntry(RepoPath repoPath) {
        return getSession().getLockEntry(repoPath);
    }

    /**
     * This method returns a mutable item only if an active session and the item is write locked by the current thread.
     * It is safe to call it without an active session (in suck case null is returned).
     *
     * @param repoPath The repository path to check
     * @return Mutable item if there's one locked by the current thread
     */
    @Nullable
    public static MutableVfsItem getIfWriteLockedByMe(RepoPath repoPath) {
        StorageSession session = StorageSessionHolder.getSession();
        if (session == null) {
            return null;
        }
        SessionLockEntry lockEntry = session.getLockEntry(repoPath);
        if (lockEntry == null) {
            return null;
        }
        return lockEntry.getMutableFsItem();
    }

}
