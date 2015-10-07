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

package org.artifactory.storage.db.fs.dao;

import com.google.common.collect.Sets;
import org.artifactory.storage.db.fs.entity.TaskRecord;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * A data access table for the tasks table.
 *
 * @author Yossi Shaul
 */
@Repository
public class TasksDao extends BaseDao {

    @Autowired
    public TasksDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    @Nonnull
    public Set<TaskRecord> load(String taskType) throws SQLException {
        ResultSet resultSet = null;
        Set<TaskRecord> entries = Sets.newLinkedHashSet();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT * FROM tasks WHERE task_type = ?", taskType);
            while (resultSet.next()) {
                entries.add(taskFromResultSet(resultSet));
            }
            return entries;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean exist(String taskType, String taskContext) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect("SELECT COUNT(*) FROM tasks WHERE " +
                    "task_type = ? AND task_context = ?", taskType, taskContext);
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            return count > 0;

        } finally {
            DbUtils.close(resultSet);
        }
    }

    public void create(String taskType, String taskContext) throws SQLException {
        jdbcHelper.executeUpdate("INSERT INTO tasks VALUES(?, ?)", taskType, taskContext);
    }

    public boolean delete(String taskType, String taskContext) throws SQLException {
        return jdbcHelper.executeUpdate("DELETE FROM tasks WHERE task_type = ? AND task_context = ?",
                taskType, taskContext) > 0;
    }

    private TaskRecord taskFromResultSet(ResultSet rs) throws SQLException {
        return new TaskRecord(rs.getString(1), rs.getString(2));
    }
}
