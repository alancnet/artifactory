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

package org.artifactory.storage.db.fs.itest.service;

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableStatsInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.testng.Assert.*;

/**
 * Integration tests for {@link org.artifactory.storage.fs.service.StatsService}.
 *
 * @author Yossi Shaul
 */
@Test
public class StatsServiceImplTest extends DbBaseTest {

    @Autowired
    private StatsService statsService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void getStatsFileWithStats() {
        StatsInfo stats = statsService.getStats(new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar"));
        assertNotNull(stats);
        assertEquals(stats.getDownloadCount(), 2);
        assertEquals(stats.getLastDownloaded(), 1340283207850L);
        assertEquals(stats.getLastDownloadedBy(), "ariels");
    }

    public void getStatsFileWithNoStats() {
        assertNull(statsService.getStats(new RepoPathImpl("repo2", "org/jfrog/test/test.jar")));
    }

    public void getStatsFolder() {
        assertNull(statsService.getStats(new RepoPathImpl("repo2", "org")));
    }

    public void getStatsNonExistingItem() {
        assertNull(statsService.getStats(new RepoPathImpl("repo2", "no/such/item")));
    }


    public void hasStatsFileWithStats() throws SQLException {
        assertTrue(statsService.hasStats(new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar")));
    }

    public void hasStatsFileWithoutStats() throws SQLException {
        assertFalse(statsService.hasStats(new RepoPathImpl("repo2", "org/jfrog/test/test.jar")));
    }

    public void hasStatsFolder() throws SQLException {
        assertFalse(statsService.hasStats(new RepoPathImpl("repo2", "org")), "Folders don't have stats");
    }

    public void hasStatsNonExistentPath() throws SQLException {
        assertFalse(statsService.hasStats(new RepoPathImpl("repo2", "no/such/path")));
    }

    @Test(dependsOnMethods = "getStatsFileWithNoStats")
    public void fileDownloadedOnFileWithNoStats() {
        RepoPathImpl filePath = new RepoPathImpl("repo2", "org/jfrog/test/test.jar");
        long lastDownloaded = System.currentTimeMillis();
        statsService.fileDownloaded(filePath, "talias", lastDownloaded);

        StatsInfo stats = statsService.getStats(filePath);
        assertNotNull(stats);
        assertEquals(stats.getDownloadCount(), 1);
        assertEquals(stats.getLastDownloaded(), lastDownloaded);
        assertEquals(stats.getLastDownloadedBy(), "talias");
    }

    @Test(dependsOnMethods = "fileDownloadedOnFileWithNoStats")
    public void fileDownloadedOnFileWithStats() {
        RepoPathImpl filePath = new RepoPathImpl("repo2", "org/jfrog/test/test.jar");
        long lastDownloaded = System.currentTimeMillis() + 2000;
        statsService.fileDownloaded(filePath, "ariels", lastDownloaded);

        StatsInfo stats = statsService.getStats(filePath);
        assertNotNull(stats);
        assertEquals(stats.getDownloadCount(), 2);
        assertEquals(stats.getLastDownloaded(), lastDownloaded);
        assertEquals(stats.getLastDownloadedBy(), "ariels");
    }

    public void fileDownloadedOnNonExistingItem() {
        RepoPathImpl filePath = new RepoPathImpl("repo2", "no/such/item.jhk");
        statsService.fileDownloaded(filePath, "ariels", System.currentTimeMillis());

        // file downloaded events are not checked against the database and the getStats will return the cached result
        StatsInfo stats = statsService.getStats(filePath);
        assertNotNull(stats);
        assertTrue(statsService.hasStats(filePath));

        // but the db should return null
        StatsInfo statsFromDb = ReflectionTestUtils.invokeMethod(statsService, "getStatsFromStorage", filePath);
        assertNull(statsFromDb);

        // and after flushing the events from memory, getStats should also return null
        ReflectionTestUtils.invokeMethod(statsService, "flushStats");
        assertNull(statsService.getStats(filePath), "No stats for no-existent file expected after flush");
        assertFalse(statsService.hasStats(filePath));
    }

    public void setStatsOnFileWithoutStats() {
        RepoPathImpl filePath = new RepoPathImpl("repo2", "org/jfrog/test/test2.jar");
        assertNull(statsService.getStats(filePath));

        long lastDownloaded = System.currentTimeMillis();
        MutableStatsInfo statsInfo = InfoFactoryHolder.get().createStats();
        statsInfo.setDownloadCount(888);
        statsInfo.setLastDownloaded(lastDownloaded);
        statsInfo.setLastDownloadedBy("yossis");
        statsService.setStats(505, statsInfo);

        StatsInfo stats = statsService.getStats(filePath);
        assertNotNull(stats);
        assertEquals(stats.getDownloadCount(), 888);
        assertEquals(stats.getLastDownloaded(), lastDownloaded);
        assertEquals(stats.getLastDownloadedBy(), "yossis");
    }

    @Test(dependsOnMethods = "setStatsOnFileWithoutStats")
    public void setStatsOnFileWithStats() {
        RepoPathImpl filePath = new RepoPathImpl("repo2", "org/jfrog/test/test2.jar");
        assertNotNull(statsService.getStats(filePath));

        long lastDownloaded = System.currentTimeMillis() + 3000;
        MutableStatsInfo statsInfo = InfoFactoryHolder.get().createStats();
        statsInfo.setDownloadCount(999);
        statsInfo.setLastDownloaded(lastDownloaded);
        statsInfo.setLastDownloadedBy("talias");
        statsService.setStats(505, statsInfo);

        StatsInfo stats = statsService.getStats(filePath);
        assertNotNull(stats);
        assertEquals(stats.getDownloadCount(), 999);
        assertEquals(stats.getLastDownloaded(), lastDownloaded);
        assertEquals(stats.getLastDownloadedBy(), "talias");
    }

    @Test(dependsOnMethods = "setStatsOnFileWithStats")
    public void deleteStatsOnFileWithStats() {
        RepoPathImpl filePath = new RepoPathImpl("repo2", "org/jfrog/test/test2.jar");
        assertNotNull(statsService.getStats(filePath));

        boolean deleted = statsService.deleteStats(505);
        assertTrue(deleted);
        assertNull(statsService.getStats(filePath));
    }

    @Test(dependsOnMethods = "deleteStatsOnFileWithStats")
    public void deleteStatsOnFileWithoutStats() {
        assertFalse(statsService.deleteStats(505), "Stats already deleted earlier");
    }

    public void deleteStatsOnNonExistingNode() {
        assertFalse(statsService.deleteStats(64736));
    }
}
