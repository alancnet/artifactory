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

import org.artifactory.storage.db.fs.dao.WatchesDao;
import org.artifactory.storage.db.fs.entity.Watch;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.storage.db.fs.dao.WatchesDao}.
 *
 * @author Yossi Shaul
 */
public class WatchesDaoTest extends DbBaseTest {

    @Autowired
    private WatchesDao watchesDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void hasWatchesNodeWithWatches() throws SQLException {
        boolean result = watchesDao.hasWatches(4);
        assertTrue(result, "Node expected to hold watches");
    }

    public void hasWatchesNodeWithoutWatches() throws SQLException {
        boolean result = watchesDao.hasWatches(1);
        assertFalse(result, "Node is not expected to hold watches");
    }

    public void hasWatchesNodeNotExist() throws SQLException {
        boolean result = watchesDao.hasWatches(5478939);
        assertFalse(result, "Node that doesn't exist is not expected to hold watches");
    }

    public void getWatchesNodeWithWatches() throws SQLException {
        List<Watch> result = watchesDao.getWatches(4);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        for (Watch watch : result) {
            assertEquals(watch.getNodeId(), 4, "All results should be with the same node id");
        }

        Watch buildName = getByUsername("scott", result);
        assertEquals(buildName.getWatchId(), 1);
        assertEquals(buildName.getSince(), 1340286203555L);
        assertEquals(buildName.getUsername(), "scott");

        buildName = getByUsername("amy", result);
        assertEquals(buildName.getWatchId(), 2);
        assertEquals(buildName.getSince(), 1340286203666L);
        assertEquals(buildName.getUsername(), "amy");
    }

    public void getWatchesNodeWithoutWatches() throws SQLException {
        List<Watch> result = watchesDao.getWatches(1);
        assertEquals(result.size(), 0);
    }

    public void getWatchesNodeNotExist() throws SQLException {
        List<Watch> result = watchesDao.getWatches(98958459);
        assertEquals(result.size(), 0);
    }

    public void insertWatch() throws SQLException {
        long time = System.currentTimeMillis();
        int createCount = watchesDao.create(new Watch(11, 6, "yoyo", time - 1000));
        assertEquals(createCount, 1);
        createCount = watchesDao.create(new Watch(12, 6, "lolo", time));
        assertEquals(createCount, 1);

        List<Watch> watches = watchesDao.getWatches(6);
        assertEquals(watches.size(), 2);

        Watch buildName = getByUsername("lolo", watches);
        assertEquals(buildName.getWatchId(), 12);
        assertEquals(buildName.getNodeId(), 6);
        assertEquals(buildName.getSince(), time);
        assertEquals(buildName.getUsername(), "lolo");

    }

    public void deleteWatchesNodeWithWatches() throws SQLException {
        // first check the Watches exist
        assertEquals(watchesDao.getWatches(9).size(), 2);

        int deletedCount = watchesDao.deleteWatches(9);
        assertEquals(deletedCount, 2);
        assertEquals(watchesDao.getWatches(9).size(), 0);
    }

    public void deleteWatchesNodeWithNoWatches() throws SQLException {
        assertEquals(watchesDao.deleteWatches(1), 0);
    }

    public void deleteWatchesNonExistentNode() throws SQLException {
        assertEquals(watchesDao.deleteWatches(6778678), 0);
    }

    public void deleteUserWatches() throws SQLException {
        assertEquals(watchesDao.deleteUserWatches(10, "dodo"), 1);
    }

    public void deleteAllUserWatches() throws SQLException {
        assertEquals(watchesDao.deleteAllUserWatches("momo"), 2);
    }

    public void deleteAllUserWatchesNoSuchUser() throws SQLException {
        assertEquals(watchesDao.deleteAllUserWatches("nosuchuser"), 0);
    }

    private Watch getByUsername(String username, List<Watch> Watches) {
        for (Watch watch : Watches) {
            if (watch.getUsername().equals(username)) {
                return watch;
            }
        }
        return null;
    }
}
