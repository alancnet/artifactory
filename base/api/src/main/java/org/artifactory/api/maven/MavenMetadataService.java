package org.artifactory.api.maven;

import org.artifactory.api.repo.Async;
import org.artifactory.repo.RepoPath;

import java.util.Set;

/**
 * A service for calculating maven metadata.
 *
 * @author Yossi Shaul
 */
public interface MavenMetadataService {

    /**
     * Calculate maven metadata on the giver repo path asynchronously.
     *
     * @param baseFolderPath A path to a folder to start calculating metadata from. Must be a local non-cache repository
     *                       path.
     * @param recursive      True is should calculate recursively from the base folder path
     */
    @Async(delayUntilAfterCommit = true, transactional = false)
    void calculateMavenMetadataAsync(RepoPath baseFolderPath, boolean recursive);

    /**
     * Calculate maven metadata on the given set of repo paths asynchronously non-recursive on each repo path.
     *
     * @param baseFolderPaths A set of folders to start calculating metadata from. Must be a local non-cache
     * @param recursive       True is should calculate recursively from each of the base folder path
     */
    @Async(delayUntilAfterCommit = true, transactional = false)
    void calculateMavenMetadataAsyncNonRecursive(Set<RepoPath> baseFolderPaths);

    /**
     * Calculates the maven metadata recursively on all the folders under the input folder.
     * This will also trigger asynchronous maven metadata calculation for maven plugins.
     *
     * @param baseFolderPath Base repo path to start calculating from
     * @param recursive      True is should calculate recursively from the base folder path
     */
    void calculateMavenMetadata(RepoPath baseFolderPath, boolean recursive);

    /**
     * Calculate the maven plugins metadata asynchronously after the current transaction is committed. The reason is the
     * metadata calculator uses xpath queries for its job and since the move is not committed yet, the xpath query
     * result might not be accurate (for example when moving plugins from one repo to another the query on the source
     * repository will return the moved plugins while the target repo will not return them). <p/> Note: you should call
     * the markBaseForMavenMetadataRecalculation() before calling this method to recover in case this task is
     * interrupted in the middle.
     *
     * @param localRepoKey Key of the local non-cache repository to calculate maven plugins metadata on.
     */
    @Async(delayUntilAfterCommit = true)
    void calculateMavenPluginsMetadataAsync(String localRepoKey);

}
