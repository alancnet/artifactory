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

package org.artifactory.storage.fs.service;

import org.artifactory.fs.ZipEntryInfo;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * A business service to interact with the archive entries table.
 *
 * @author Yossi Shaul
 */
public interface ArchiveEntriesService {

    /**
     * @param archiveSha1 The checksum of the indexed archive
     * @return Set of the indexed entries for the given sha1
     */
    @Nonnull
    Set<ZipEntryInfo> getArchiveEntries(String archiveSha1);

    /**
     * @param archiveSha1 Checksum to check entries for
     * @return True if there are entries for the given sha1 checksum
     */
    boolean isIndexed(String archiveSha1);

    /**
     * Create new archive entries for the given sha1 checksum
     *
     * @param archiveSha1 The checksum of the indexed archive
     * @param entries     The entries to add
     */
    void addArchiveEntries(String archiveSha1, Set<? extends ZipEntryInfo> entries);

    /**
     * @param archiveSha1 The checksum to delete entries for
     * @return True if any entries were deleted
     */
    boolean deleteArchiveEntries(String archiveSha1);

    /**
     * Deletes all the unreferenced archive entry paths. Archive entry paths used in a many-to-many relationship and
     * should be cleaned up periodically.
     */
    int deleteUnusedPathIds();

    /**
     * Deletes all the unreferenced archive entry names. Archive entry names used in a many-to-many relationship and
     * should be cleaned up periodically.
     */
    int deleteUnusedNameIds();
}
