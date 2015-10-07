package org.artifactory.addon.gems;

import org.artifactory.addon.Addon;
import org.artifactory.api.repo.Async;

/**
 * @author mamo
 */
public interface GemsAddon extends Addon {

    @Async
    void reindexAsync(String repoKey);

    @Async(delayUntilAfterCommit = true, authenticateAsSystem = true)
    void afterRepoInit(String repoKey);

    ArtifactGemsInfo getGemsInfo(String repoKey,String path);
}
