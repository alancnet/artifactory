package org.artifactory.addon.nuget;

import org.artifactory.addon.Addon;
import org.artifactory.nuget.NuMetaData;
import org.artifactory.repo.RepoPath;

/**
 * @author Chen Keinan
 */
public interface UiNuGetAddon extends Addon {

    NuMetaData getNutSpecMetaData(RepoPath nuGetRepoPath);

    void requestAsyncReindexNuPkgs(String repoKey);
}