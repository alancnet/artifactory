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
 * Service to support folders pruning in a separate transaction asynchronously.
 *
 * @author Yossi Shaul
 */
public interface FolderPruningService extends ReloadableBean {

    /**
     * Adds the repo path to the prune queue and prunes all the folders in the queue.
     *
     * @param folderRepoPath Repo path of a folder to prune (to add to the queue)
     */
    void prune(RepoPath folderRepoPath);

}
