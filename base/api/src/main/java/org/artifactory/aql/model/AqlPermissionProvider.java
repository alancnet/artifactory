package org.artifactory.aql.model;

import org.artifactory.repo.RepoPath;

/**
 * @author Gidi Shabat
 */
public interface AqlPermissionProvider {
    boolean canRead(RepoPath repoPath);

    boolean isAdmin();

    boolean isOss();
}
