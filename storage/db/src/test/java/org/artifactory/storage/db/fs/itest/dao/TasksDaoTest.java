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

package org.artifactory.storage.db.fs.itest.dao;

import org.artifactory.storage.db.fs.dao.TasksDao;
import org.artifactory.storage.db.fs.entity.TaskRecord;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.db.fs.dao.TasksDao}.
 *
 * @author Yossi Shaul
 */
public class TasksDaoTest extends DbBaseTest {

    @Autowired
    private TasksDao tasksDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void loadByType() throws SQLException {
        Set<TaskRecord> tasks = tasksDao.load("INDEX");
        assertNotNull(tasks);
        assertEquals(tasks.size(), 2);
        for (TaskRecord task : tasks) {
            assertEquals(task.getTaskType(), "INDEX");
        }
    }

    public void exist() throws SQLException {
        assertTrue(tasksDao.exist("INDEX", "repo1:ant/ant/1.5/ant-1.5.jar"));
        assertFalse(tasksDao.exist("INDEX", "repo1:ant/ant/1.9/ant-1.9.jar"));
    }

    public void create() throws SQLException {
        assertFalse(tasksDao.exist("MYTEST", "123"));
        tasksDao.create("MYTEST", "123");
        Set<TaskRecord> myTestTasks = tasksDao.load("MYTEST");
        assertEquals(myTestTasks.size(), 1);
        assertEquals(myTestTasks.iterator().next().getTaskType(), "MYTEST");
        assertEquals(myTestTasks.iterator().next().getTaskContext(), "123");
    }

    @Test
    public void createDuplicate() throws SQLException {
        // until decided otherwise the unique constraint is removed
        tasksDao.create("DUPLICATE", "DUP");
        tasksDao.create("DUPLICATE", "DUP");
    }

    public void delete() throws SQLException {
        assertTrue(tasksDao.exist("MMC", "this/is/a/test"));
        assertTrue(tasksDao.delete("MMC", "this/is/a/test"));
        assertFalse(tasksDao.exist("MMC", "this/is/a/test"));
    }

}
