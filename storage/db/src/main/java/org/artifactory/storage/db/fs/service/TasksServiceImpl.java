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

package org.artifactory.storage.db.fs.service;

import com.google.common.collect.Sets;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.fs.dao.TasksDao;
import org.artifactory.storage.db.fs.entity.TaskRecord;
import org.artifactory.storage.fs.service.TasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Set;

/**
 * A business service to interact with the tasks table.
 *
 * @author Yossi Shaul
 */
@Service
public class TasksServiceImpl implements TasksService {

    @Autowired
    private TasksDao tasksDao;

    @Override
    @Nonnull
    public Set<RepoPath> getIndexTasks() {
        return getRepoPathTasks(TASK_TYPE_INDEX);
    }

    @Nonnull
    private Set<RepoPath> getRepoPathTasks(String type) {
        // this method expects repo path id as the task value
        Set<RepoPath> repoPaths = Sets.newLinkedHashSet();
        try {
            Set<TaskRecord> tasks = tasksDao.load(type);
            for (TaskRecord task : tasks) {
                repoPaths.add(InternalRepoPathFactory.createRepoPath(task.getTaskContext()));
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to load tasks of type '" + type + "' : " + e.getMessage(), e);
        }
        return repoPaths;
    }

    @Override
    public boolean hasIndexTask(RepoPath repoPath) {
        try {
            return tasksDao.exist(TASK_TYPE_INDEX, repoPath.getId());
        } catch (SQLException e) {
            throw new StorageException("Failed to check index task for " + repoPath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void addIndexTask(RepoPath repoPath) {
        try {
            if (!hasIndexTask(repoPath)) {
                tasksDao.create(TASK_TYPE_INDEX, repoPath.getId());
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to add index task for " + repoPath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeIndexTask(RepoPath repoPath) {
        try {
            return tasksDao.delete(TASK_TYPE_INDEX, repoPath.getId());
        } catch (SQLException e) {
            throw new StorageException("Failed to delete index task for " + repoPath + ": " + e.getMessage(), e);
        }
    }

}
