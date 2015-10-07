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

package org.artifactory.storage.db.fs.session;

import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.lock.FsItemLockEntry;
import org.artifactory.storage.fs.lock.LockEntryId;
import org.artifactory.storage.fs.lock.SessionLockEntry;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.tx.SessionResource;
import org.artifactory.storage.tx.SessionResourceManager;
import org.artifactory.storage.tx.SessionResourceManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a thread bound storage session in a context of a transaction.
 *
 * @author Yossi Shaul
 */
public class SqlStorageSession implements StorageSession {
    private static final Logger log = LoggerFactory.getLogger(SqlStorageSession.class);

    private final Map<RepoPath, SessionLockEntry> locks = new LinkedHashMap<>();
    private SessionResourceManager sessionResourceManager;

    @Override
    public void save() {
        if (sessionResourceManager != null) {
            sessionResourceManager.onSessionSave();
        }

        if (locks.size() == 0) {
            log.trace("Save called on session with no locked items");
            return;
        }

        log.debug("Save called on session with {} locked items", locks.size());

        Iterator<Map.Entry<RepoPath, SessionLockEntry>> locksIter = locks.entrySet().iterator();
        while (locksIter.hasNext()) {
            SessionLockEntry lockEntry = locksIter.next().getValue();
            if (lockEntry.isWriteLockedByMe()) {
                lockEntry.save();
                if (lockEntry.isDeleted()) {
                    // deleted items are removed immediately from the session to support simpler override during move/copy
                    lockEntry.unlock();
                    locksIter.remove();
                }
            }
        }

    }

    @Override
    public void releaseResources() {
        log.trace("Release all locks of lm={}", this.hashCode());
        try {
            Collection<SessionLockEntry> lockEntries = locks.values();
            for (SessionLockEntry lockEntry : lockEntries) {
                lockEntry.unlock();
            }
        } finally {
            locks.clear();
        }
    }

    @Override
    public FsItemLockEntry writeLock(LockEntryId lockEntryId) {
        SessionLockEntry sessionLockEntry = getOrCreateSessionLockEntry(lockEntryId);
        sessionLockEntry.acquireWriteLock();
        return sessionLockEntry;
    }

    @Override
    public boolean removeLockEntry(RepoPath repoPath) {
        SessionLockEntry lockEntry = locks.remove(repoPath);
        if (lockEntry == null) {
            log.debug("Removing lock entry {} but not locked by me!", repoPath);
            return false;
        } else {
            log.trace("Removed lock entry {} for lm={}", repoPath, this.hashCode());
            lockEntry.unlock();
            return true;
        }
    }

    @Override
    @Nullable
    public SessionLockEntry getLockEntry(RepoPath repoPath) {
        return locks.get(repoPath);
    }

    private SessionLockEntry getOrCreateSessionLockEntry(LockEntryId lockEntryId) {
        RepoPath repoPath = lockEntryId.getRepoPath();
        SessionLockEntry sessionLockEntry = getLockEntry(repoPath);
        if (sessionLockEntry == null) {
            log.trace("Creating new SLE for {} in lm={}", repoPath, this.hashCode());
            sessionLockEntry = new SessionLockEntry(lockEntryId);
            locks.put(repoPath, sessionLockEntry);
        } else {
            log.trace("Reusing existing SLE for {} in lm={}", repoPath, this.hashCode());
        }
        return sessionLockEntry;
    }

    @Override
    public <T extends SessionResource> T getOrCreateResource(Class<T> resourceClass) {
        return getSessionResourceManager().getOrCreateResource(resourceClass);
    }

    @Override
    public void afterCompletion(boolean success) {
        if (sessionResourceManager != null) {
            sessionResourceManager.afterCompletion(success);
        }
    }

    public SessionResourceManager getSessionResourceManager() {
        if (sessionResourceManager == null) {
            sessionResourceManager = new SessionResourceManagerImpl();
        }
        return sessionResourceManager;
    }
}
