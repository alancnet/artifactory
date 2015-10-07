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

import org.artifactory.repo.RepoPath;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * A business service to interact with the tasks table.
 *
 * @author Yossi Shaul
 */
public interface TasksService {

    /**
     * Unique name of the archive indexing task type
     */
    String TASK_TYPE_INDEX = "INDEX";

    /**
     * @return All the repo paths currently pending for indexing.
     */
    @Nonnull
    Set<RepoPath> getIndexTasks();

    /**
     * @param repoPath The repo path to check
     * @return True if there is a pending index request for this checksum
     */
    boolean hasIndexTask(RepoPath repoPath);

    /**
     * Adds an index task for the given repo path.
     *
     * @param repoPath The repo path to index
     */
    void addIndexTask(RepoPath repoPath);

    /**
     * Removes an index task.
     *
     * @param repoPath The repo path to remove
     * @return True if removed from the database.
     */
    boolean removeIndexTask(RepoPath repoPath);

}
