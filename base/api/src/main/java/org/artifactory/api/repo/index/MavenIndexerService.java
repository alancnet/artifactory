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

package org.artifactory.api.repo.index;

import org.artifactory.common.MutableStatusHolder;

import java.util.List;

/**
 * @author yoavl
 */
public interface MavenIndexerService {

    /**
     * Schedule the indexer to run immediately even if it is disabled
     */
    void scheduleImmediateIndexing(MutableStatusHolder statusHolder);

    /**
     * Run the indexer immediately for a specific repo even if it's not included
     *
     * @param statusHolder        holds the status of the started task which runs the indexer task
     * @param repoKeys            Keys of repositories to index
     * @param forceRemoteDownload force remote repo index download even if there is one at the cache and even if not
     */
    void runSpecificIndexer(MutableStatusHolder statusHolder, List<String> repoKeys, boolean forceRemoteDownload);
}