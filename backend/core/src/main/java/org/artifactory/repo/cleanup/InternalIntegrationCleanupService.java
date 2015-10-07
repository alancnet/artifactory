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

package org.artifactory.repo.cleanup;

import org.artifactory.repo.RepoPath;
import org.artifactory.spring.ReloadableBean;

/**
 * The main internal maven unique snapshots cleanup service
 *
 * @author Shay Yaakov
 */
public interface InternalIntegrationCleanupService extends ReloadableBean {

    /**
     * Performs the actual clean.
     * This method is protected by a semaphore as only one thread is allowed entering here.
     */
    void clean();

    /**
     * Adds the given {@link RepoPath} parent to the cache so that the job reading this queue will perform the clean on it.
     *
     * @param fileRepoPath The file repo path which is later used to calculate module info from
     */
    void addItemToCache(RepoPath fileRepoPath);
}
