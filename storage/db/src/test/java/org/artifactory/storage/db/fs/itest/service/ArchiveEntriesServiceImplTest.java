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

import com.google.common.collect.Sets;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.model.xstream.fs.ZipEntryImpl;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryData;
import org.artifactory.storage.db.fs.dao.ArchiveEntriesDao;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.fs.service.ArchiveEntriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link org.artifactory.storage.fs.service.ArchiveEntriesService}
 *
 * @author Yossi Shaul
 */
@Test
public class ArchiveEntriesServiceImplTest extends DbBaseTest {

    @Autowired
    private ArchiveEntriesService archiveEntriesService;

    @Autowired
    private ArchiveEntriesDao archiveEntriesDao;

    @Autowired
    private BinariesDao binariesDao;

    @Autowired
    private JdbcHelper jdbcHelper;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void hasEntries() {
        assertTrue(archiveEntriesService.isIndexed("dcab88fc2a043c2479a6de676a2f8179e9ea2167"));
    }

    public void hasEntriesNoSuchChecksum() {
        assertFalse(archiveEntriesService.isIndexed("999988fc2a043c2479a6de676a2f8179e9e55555"));
    }

    public void getEntries() {
        Set<ZipEntryInfo> entries = archiveEntriesService.getArchiveEntries("dcab88fc2a043c2479a6de676a2f8179e9ea2167");
        assertThat(entries).hasSize(3).doesNotHaveDuplicates()
                .containsOnly(
                        new ZipEntryImpl("META-INF/LICENSE.txt", false),
                        new ZipEntryImpl("META-INF/MANIFEST.MF", false),
                        new ZipEntryImpl("org/apache/tools/ant/filters/BaseFilterReader.class", false));
    }

    public void insertEntries() {
        Set<ZipEntryImpl> entriesToInsert = Sets.newHashSet(
                new ZipEntryImpl("my/test.me", false),
                new ZipEntryImpl("my/test2", false),
                new ZipEntryImpl("my/test/MyClass.class", false)
        );

        String sha1 = "ecab88fc2a043c2479a6de676a2f8179e9ea2167";
        assertFalse(archiveEntriesService.isIndexed(sha1));
        archiveEntriesService.addArchiveEntries(sha1, entriesToInsert);
        assertTrue(archiveEntriesService.isIndexed(sha1));
        Set<ZipEntryInfo> entriesLoaded = archiveEntriesService.getArchiveEntries(sha1);
        assertThat(entriesLoaded).hasSize(entriesToInsert.size()).isEqualTo(entriesToInsert);
    }

    @Test(dependsOnMethods = "insertEntries")
    public void deleteEntries() throws SQLException {
        String sha1 = "ecab88fc2a043c2479a6de676a2f8179e9ea2167";
        assertThat(archiveEntriesDao.findIndexedArchiveIdByChecksum(sha1)).isNotEqualTo(DbService.NO_DB_ID);
        assertTrue(archiveEntriesService.deleteArchiveEntries(sha1));
        assertFalse(archiveEntriesService.isIndexed(sha1));
        assertThat(archiveEntriesDao.findIndexedArchiveIdByChecksum(sha1)).isEqualTo(DbService.NO_DB_ID);
    }

    public void deleteEntriesNotIndexed() {
        assertFalse(archiveEntriesService.deleteArchiveEntries("999988fc2a043c2479a6de676a2f8179e9e55566"));
    }

    public void insertEntriesReusedPathAndName() throws SQLException {
        Set<ZipEntryImpl> firstArchiveEntries = Sets.newHashSet(
                new ZipEntryImpl("path1/name1", false),
                new ZipEntryImpl("path1/name2", false)
        );
        String firstSha1 = randomSha1();
        binariesDao.create(new BinaryData(firstSha1, randomMd5(), 789));

        // assert that before inserting there are no path and name ids
        assertThat(archiveEntriesDao.findArchivePathId("path1")).isEqualTo(DbService.NO_DB_ID);
        assertThat(archiveEntriesDao.findArchiveNameId("name1")).isEqualTo(DbService.NO_DB_ID);

        archiveEntriesService.addArchiveEntries(firstSha1, firstArchiveEntries);

        // now there should be path and name ids
        assertThat(archiveEntriesDao.findArchivePathId("path1")).isNotEqualTo(DbService.NO_DB_ID);
        assertThat(archiveEntriesDao.findArchiveNameId("name1")).isNotEqualTo(DbService.NO_DB_ID);

        assertTrue(archiveEntriesService.isIndexed(firstSha1));
        Set<ZipEntryInfo> entriesLoaded = archiveEntriesService.getArchiveEntries(firstSha1);
        assertThat(entriesLoaded).hasSize(firstArchiveEntries.size()).isEqualTo(firstArchiveEntries);

        // now lets insert additional archive with similar paths and names
        Set<ZipEntryImpl> secondArchiveEntries = Sets.newHashSet(
                new ZipEntryImpl("path1/name1", false),
                new ZipEntryImpl("path2/name2", false),
                new ZipEntryImpl("path3/name2", false)
        );
        String secondSha1 = randomSha1();
        binariesDao.create(new BinaryData(secondSha1, randomMd5(), 4512));

        archiveEntriesService.addArchiveEntries(secondSha1, secondArchiveEntries);
        assertThat(archiveEntriesService.getArchiveEntries(secondSha1)).hasSize(secondArchiveEntries.size())
                .isEqualTo(secondArchiveEntries);
    }

    public void deleteKeepsPathAndName() throws SQLException {
        // test that deleting an archive entries doesn't remove the path and name. Only GC cleans the unused
        String archiveSha1 = randomSha1();
        binariesDao.create(new BinaryData(archiveSha1, randomMd5(), 56));

        assertThat(archiveEntriesDao.findArchivePathId("uniquepath")).isEqualTo(DbService.NO_DB_ID);
        assertThat(archiveEntriesDao.findArchiveNameId("uniquename")).isEqualTo(DbService.NO_DB_ID);

        archiveEntriesService.addArchiveEntries(archiveSha1,
                Sets.newHashSet(new ZipEntryImpl("uniquepath/uniquename", true)));

        assertThat(archiveEntriesDao.findArchivePathId("uniquepath")).isNotEqualTo(DbService.NO_DB_ID);
        assertThat(archiveEntriesDao.findArchiveNameId("uniquename")).isNotEqualTo(DbService.NO_DB_ID);
    }

}
