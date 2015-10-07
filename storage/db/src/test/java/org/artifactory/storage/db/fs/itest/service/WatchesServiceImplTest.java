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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.artifactory.fs.WatcherInfo;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.WatcherImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.entity.Watch;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.WatchesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Yossi Shaul
 */
@Test
public class WatchesServiceImplTest extends DbBaseTest {

    @Autowired
    private WatchesService watchesService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void getWatchesNodeWithWatches() {
        WatchersInfo watches = watchesService.getWatches(new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar"));

        assertEquals(watches.getWatchers().size(), 1);
        WatcherInfo watch = watches.getWatcher("scott");
        assertNotNull(watch);
        assertEquals(watch.getUsername(), "scott");
        assertEquals(watch.getWatchingSinceTime(), 1340286203555L);
    }

    public void getWatchesNodeWithMultiWatches() {
        WatchersInfo watches = watchesService.getWatches(new RepoPathImpl("repo1", "org/yossis"));
        assertEquals(watches.getWatchers().size(), 2);
    }

    public void getWatchesNodeWithNone() {
        assertEquals(watchesService.getWatches(new RepoPathImpl("repo1", "ant")).getWatchers().size(), 0);
    }

    public void getWatchesNodeNotExist() {
        assertEquals(watchesService.getWatches(new RepoPathImpl("repo1", "not/exists/bla")).getWatchers().size(), 0);
    }

    public void createWatch() {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "org");
        assertEquals(watchesService.getWatches(repoPath).getWatchers().size(), 0);
        long since = System.currentTimeMillis();
        watchesService.addWatch(6, new WatcherImpl("adi", since));

        WatchersInfo watches = watchesService.getWatches(repoPath);
        assertEquals(watches.getWatchers().size(), 1);
        assertEquals(watches.getWatcher("adi").getUsername(), "adi");
        assertEquals(watches.getWatcher("adi").getWatchingSinceTime(), since);
    }

    @Test(dependsOnMethods = "createWatch")
    public void deleteWatches() {
        int count = watchesService.deleteWatches(6);
        assertEquals(count, 1);

        assertEquals(watchesService.getWatches(new RepoPathImpl("repo1", "org")).getWatchers().size(), 0);
    }

    public void createWatches() {
        RepoPathImpl repoPath = new RepoPathImpl("repo2", "");
        assertEquals(watchesService.getWatches(repoPath).getWatchers().size(), 0);
        long since = System.currentTimeMillis();
        List<WatcherInfo> watches = Lists.<WatcherInfo>newArrayList(
                new WatcherImpl("a", since), new WatcherImpl("b", since));
        watchesService.addWatches(500, watches);

        assertEquals(watchesService.getWatches(repoPath).getWatchers().size(), 2);
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = "createWatches")
    public void verifyCachePopulation() throws Exception {
        Method method = ReflectionUtils.findMethod(watchesService.getClass(), "getWatchersCache");
        ReflectionUtils.makeAccessible(method);
        Multimap<RepoPath, Watch> cache = (Multimap<RepoPath, Watch>) ReflectionUtils.invokeMethod(method,
                watchesService);

        RepoPathImpl repoPath = new RepoPathImpl("repo2", "");
        assertEquals(cache.get(repoPath).size(), 2);
    }

    public void deleteUserWatches() {
        RepoPathImpl repoPath = new RepoPathImpl("repo2", "org/jfrog");
        assertEquals(watchesService.deleteUserWatches(repoPath, "yossis"), 1);
    }

    public void deleteUserWatchesNoSuchUser() {
        RepoPathImpl repoPath = new RepoPathImpl("repo2", "org/jfrog/test");
        assertEquals(watchesService.deleteUserWatches(repoPath, "nouser"), 0);
    }

    public void deleteAllUserWatches() {
        assertEquals(watchesService.deleteAllUserWatches("yoyo"), 3);
    }

    public void deleteAllUserWatchesNoSuchUser() {
        assertEquals(watchesService.deleteAllUserWatches("nouser"), 0);
    }

    public void hasWatchesNodeWithWatches() {
        assertTrue(watchesService.hasWatches(new RepoPathImpl("repo2", "org/jfrog/test")));
    }

    public void hasWatchesNodeWithoutWatches() {
        assertFalse(watchesService.hasWatches(new RepoPathImpl("repo1", "ant/ant")));
    }

    public void hasWatchesNodeNotExist() {
        assertFalse(watchesService.hasWatches(new RepoPathImpl("repo99", "ant/ant")));
    }
}
