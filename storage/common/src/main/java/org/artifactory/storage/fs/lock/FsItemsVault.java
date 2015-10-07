package org.artifactory.storage.fs.lock;

import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;

/**
 * @author mamo
 */
public interface FsItemsVault {
    @Nonnull
    LockEntryId getLock(RepoPath repoPath);
}
