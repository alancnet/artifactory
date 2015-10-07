/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.addon.bower;

import org.artifactory.addon.Addon;
import org.artifactory.api.repo.Async;
import org.artifactory.fs.FileInfo;

/**
 * Core Bower functionality interface
 *
 * @author Shay Yaakov
 */
public interface BowerAddon extends Addon {

    /**
     * Adds a bower package to the repository and indexing it by tagging name and version properties.
     *
     * @param info The added bower package file
     */
    void addBowerPackage(FileInfo info);

    /**
     *
     * Adds a bower package to the repository asynchronously, delegates to the indexing for properties extraction.
     *
     * @param info The added bower package file
     */
    @Async(delayUntilAfterCommit = true)
    void handleAddAfterCommit(FileInfo info);

    /**
     * Removes a bower package from the repository.
     *
     * @param info The bower package file to be removed
     */
    void removeBowerPackage(FileInfo info);

    /**
     * Checks if a given path is a valid bower file according to it's extension (tar.gz, tgz, zip).
     *
     * @param filePath The file path to check
     */
    boolean isBowerFile(String filePath);

    /**
     * Adds the given repository key into the map of queued reindex requests and trigger asynchronously
     * {@link BowerService#asyncReindex()} for actual re-indexing.
     *
     * @param repoKey The repository key to reindex
     */
    void requestAsyncReindexBowerPackages(String repoKey);

    /**
     * get bower meta data info
     *
     * @param fileInfo - bower file info
     * @return boower meta data info
     */
    BowerMetadataInfo getBowerMetadata(FileInfo fileInfo);
}