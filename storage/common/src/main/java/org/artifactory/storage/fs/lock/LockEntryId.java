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
import org.artifactory.storage.fs.lock.provider.LockWrapper;

/**
 * An immutable lock holder - holds a RWLock per a certain repo fsItem. RW-locks are managed (shared) per storing repo
 * and are passed to each session's InternalLockManager. On the InternalLockManager each session performs read-write
 * lock/release operations on the saved locks.
 *
 * @author freds
 * @date Oct 19, 2008
 */
public class LockEntryId {
    /**
     * The unique lock from the lock maps for the repo path of the items
     */
    private final LockWrapper lock;
    private final RepoPath repoPath;

    public LockEntryId(LockWrapper lock, RepoPath repoPath) {
        if (lock == null) {
            throw new IllegalArgumentException("Cannot create lock entry with no lock object for " + repoPath);
        }
        this.lock = lock;
        this.repoPath = repoPath;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public LockWrapper getLock() {
        return lock;
    }

    @Override
    public String toString() {
        return "LockEntryId " + repoPath;
    }
}