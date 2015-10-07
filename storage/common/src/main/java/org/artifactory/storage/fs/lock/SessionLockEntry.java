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

import org.artifactory.common.ConstantValues;
import org.artifactory.concurrent.LockingException;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.MutableVfsItem;
import org.artifactory.storage.fs.lock.provider.LockWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A session bound lock entry for a single {@link org.artifactory.repo.RepoPath}.
 *
 * @author Yossi Shaul
 */
public class SessionLockEntry implements FsItemLockEntry {
    private static final Logger log = LoggerFactory.getLogger(SessionLockEntry.class);

    private final LockEntryId lockEntryId;
    private MutableVfsItem mutableItem;

    public SessionLockEntry(LockEntryId lockEntryId) {
        this.lockEntryId = lockEntryId;
    }

    @Override
    public MutableVfsItem getMutableFsItem() {
        return mutableItem;
    }

    @Override
    public void setWriteFsItem(MutableVfsItem mutableItem) {
        this.mutableItem = mutableItem;
    }

    @Override
    public void unlock() {
        releaseWriteLock();
    }

    @Override
    public void save() {
        log.trace("Saving lock entry {}", getRepoPath());
        if (!isWriteLockedByMe()) {
            throw new LockingException("Cannot save item " + lockEntryId + " which not locked by me!");
        }
        if (mutableItem == null) {
            throw new IllegalStateException("Cannot save item " + lockEntryId + ". Mutable item is null.");
        }
        if (mutableItem.hasPendingChanges()) {
            log.debug("Saving item: {}", getRepoPath());
            mutableItem.save();
        } else {
            log.trace("Item {} has no pending changes", getRepoPath());
        }
    }

    public void acquireWriteLock() {
        log.trace("Acquiring WRITE lock on {}", lockEntryId);
        if (isWriteLockedByMe()) {
            // current thread already holds the write lock
            return;
        }

        acquire();
    }

    private void acquire() {
        LockWrapper lock = lockEntryId.getLock();
        try {
            boolean success = lock.tryLock(ConstantValues.locksTimeoutSecs.getLong(), TimeUnit.SECONDS);
            if (!success) {
                StringBuilder messageBuilder =
                        new StringBuilder().append("Lock on ").append(lockEntryId)
                                .append(" not acquired in ").append(ConstantValues.locksTimeoutSecs.getLong())
                                .append(" seconds. Lock info: ").append(lock).append(".");
                if (ConstantValues.locksDebugTimeouts.getBoolean()) {
                    LockingDebugUtils.debugLocking(lockEntryId, messageBuilder);
                }
                throw new LockingException(messageBuilder.toString());
            }
        } catch (InterruptedException e) {
            throw new LockingException("Lock on " + lockEntryId + " not acquired!", e);
        }
    }

    public boolean releaseWriteLock() {
        log.trace("Releasing WRITE lock on {}", lockEntryId);
        if (isWriteLockedByMe()) {
            if (mutableItem != null) {
                mutableItem.releaseResources();
                if (mutableItem.hasPendingChanges()) {
                    // local modification will be discarded
                    log.warn("Mutable item '{}' has local modifications that will be discarded.", mutableItem);
                }
            }
            mutableItem = null;
            lockEntryId.getLock().unlock();
            return true;
        }
        return false;
    }

    public boolean isWriteLockedByMe() {
        return lockEntryId.getLock().isHeldByCurrentThread();
    }

    private RepoPath getRepoPath() {
        return lockEntryId.getRepoPath();
    }

    public boolean isDeleted() {
        return isWriteLockedByMe() && mutableItem.isDeleted();
    }

}
