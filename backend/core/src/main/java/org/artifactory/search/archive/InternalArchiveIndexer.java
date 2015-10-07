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

package org.artifactory.search.archive;

import org.artifactory.api.search.ArchiveIndexer;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.Lock;

/**
 * Internal interface for transaction management.
 *
 * @author Yossi Shaul
 */
public interface InternalArchiveIndexer extends ArchiveIndexer {

    /**
     * Indexes the entries of the archive in this path if it supports indexing.
     *
     * @param archiveRepoPath The path to index.
     * @return True if the file was indexed
     */
    @Lock
    boolean index(RepoPath archiveRepoPath);

    void triggerQueueIndexing();
}
