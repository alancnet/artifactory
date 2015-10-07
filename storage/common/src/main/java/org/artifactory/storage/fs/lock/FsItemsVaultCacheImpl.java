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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.lock.provider.LockProvider;
import org.artifactory.storage.fs.lock.provider.LockWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Holds all the fs item locks for a single storing repository.
 *
 * @author Yossi Shaul
 */
public class FsItemsVaultCacheImpl implements FsItemsVault {
    private static final Logger log = LoggerFactory.getLogger(FsItemsVaultCacheImpl.class);

    private LoadingCache<RepoPath, LockWrapper> locks;

    public FsItemsVaultCacheImpl(final LockProvider lockProvider) {
        locks = CacheBuilder.newBuilder().initialCapacity(2000).softValues()
                .expireAfterAccess(ConstantValues.fsItemCacheIdleTimeSecs.getLong(), TimeUnit.SECONDS)
                .build(new CacheLoader<RepoPath, LockWrapper>() {
                    @Override
                    public LockWrapper load(RepoPath key) throws Exception {
                        return lockProvider.getLock(key);
                    }
                });
    }

    /**
     * Returns a new lock entry id. Creates a lock object if not already exist.
     * This method doesn't lock anything, it will just create new lock object if not already exist.
     *
     * @param repoPath Repo path to create lock entry for
     * @return A new {@link org.artifactory.storage.fs.lock.LockEntryId}
     */
    @Override
    @Nonnull
    public LockEntryId getLock(RepoPath repoPath) {
        try {
            log.trace("Getting lock for " + repoPath);
            return new LockEntryId(locks.get(repoPath), repoPath);
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get lock from vault for " + repoPath, e);
        }
    }
}
