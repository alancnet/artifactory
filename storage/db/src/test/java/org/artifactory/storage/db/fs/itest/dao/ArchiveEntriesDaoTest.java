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

import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.dao.ArchiveEntriesDao;
import org.artifactory.storage.db.fs.entity.ArchiveEntry;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;

import java.sql.SQLException;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.storage.db.fs.dao.ArchiveEntriesDao}.
 *
 * @author Yossi Shaul
 */
public class ArchiveEntriesDaoTest extends DbBaseTest {

    @Autowired
    private ArchiveEntriesDao archiveEntriesDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void loadByChecksum() throws SQLException {
        String sha1 = "dcab88fc2a043c2479a6de676a2f8179e9ea2167";
        Set<ArchiveEntry> entries = archiveEntriesDao.loadByChecksum(sha1);
        assertNotNull(entries);
        assertEquals(entries.size(), 5);
        for (ArchiveEntry entry : entries) {
            assertEquals(entry.getArchiveSha1(), sha1);
        }

        assertTrue(entries.contains(new ArchiveEntry(sha1, "org/apache/tools/mail", "MailMessage.class")));
    }

    public void isIndexed() throws SQLException {
        assertTrue(archiveEntriesDao.isIndexed("dcab88fc2a043c2479a6de676a2f8179e9ea2167"));
    }

    public void isIndexedNoSuchChecksum() throws SQLException {
        assertFalse(archiveEntriesDao.isIndexed("abab88fc2a043c2479a6de676a2f8179e9ea5243"));
    }

    public void loadByChecksumNonExisting() throws SQLException {
        Set<ArchiveEntry> entries = archiveEntriesDao.loadByChecksum("aaab88fc2a043c2479a6de676a2f8179e9ea2167");
        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    public void removeByChecksum() throws SQLException {
        String sha1 = "bbbb88fc2a043c2479a6de676a2f8179e9eabbbb";
        assertEquals(archiveEntriesDao.loadByChecksum(sha1).size(), 2);
        int deleteCount = archiveEntriesDao.deleteByChecksum(sha1);
        assertEquals(deleteCount, 2);
        assertEquals(archiveEntriesDao.loadByChecksum(sha1).size(), 0);
    }

    public void removeByChecksumNoSuchChecksum() throws SQLException {
        int deleteCount = archiveEntriesDao.deleteByChecksum("111188fc2a043c2479a6de676a2f8179e9ea2222");
        assertEquals(deleteCount, 0);
    }

    public void getEntryPathExistingPath() throws SQLException {
        long archivePathId = archiveEntriesDao.findArchivePathId("META-INF");
        assertEquals(8001, archivePathId);
    }

    public void getEntryPathNonExistingPath() throws SQLException {
        long archivePathId = archiveEntriesDao.findArchivePathId("no/such/entry/path");
        assertEquals(DbService.NO_DB_ID, archivePathId);
    }

    public void createIndexedArchive() throws SQLException {
        String sha1 = "dddd88fc2a043c2479a6de676a2f7179e9eaddac";
        assertFalse(archiveEntriesDao.isIndexed(sha1));
        boolean created = archiveEntriesDao.createIndexedArchive(sha1, 21000);
        assertTrue(created);
        assertTrue(archiveEntriesDao.isIndexed(sha1));
    }

    public void createFindArchivePathEntry() throws SQLException {
        assertTrue(archiveEntriesDao.createArchivePath(21000, "new/path"));
        long archiveId = archiveEntriesDao.findArchivePathId("new/path");
        assertEquals(archiveId, 21000);
    }

    public void findArchivePathEntryEmpty() throws SQLException {
        // make sure empty paths are turned into single dot
        assertEquals(archiveEntriesDao.findArchivePathId(""), 8004);
        assertEquals(archiveEntriesDao.findArchivePathId("."), 8004);
    }

    public void deleteUnreferencedPathIds() throws SQLException {
        assertTrue(archiveEntriesDao.createArchivePath(21010, "delete/me"));
        assertEquals(archiveEntriesDao.findArchivePathId("delete/me"), 21010);
        assertThat(archiveEntriesDao.deleteUnusedPathIds()).isGreaterThan(0);
        assertEquals(archiveEntriesDao.findArchivePathId("delete/me"), DbService.NO_DB_ID);
    }

    public void createFindArchiveNameEntry() throws SQLException {
        assertTrue(archiveEntriesDao.createArchiveName(22000, "name.txt"));
        long nameId = archiveEntriesDao.findArchiveNameId("name.txt");
        assertEquals(nameId, 22000);
    }

    public void createFindArchiveNameEntryEmpty() throws SQLException {
        // make sure empty paths are turned into single dot
        assertTrue(archiveEntriesDao.createArchiveName(22001, ""));
        assertEquals(archiveEntriesDao.findArchiveNameId(""), 22001);
    }

    public void deleteUnreferencedNameIds() throws SQLException {
        assertTrue(archiveEntriesDao.createArchiveName(22010L, "delete.me"));
        assertEquals(archiveEntriesDao.findArchiveNameId("delete.me"), 22010L);
        assertThat(archiveEntriesDao.deleteUnusedNameIds()).isGreaterThan(0);
        assertEquals(archiveEntriesDao.findArchiveNameId("delete.me"), DbService.NO_DB_ID);
    }

    public void hasIndexedArchivesEntries() throws SQLException {
        assertTrue(archiveEntriesDao.hasIndexedArchivesEntries(6000, 8002, 9003));
        assertFalse(archiveEntriesDao.hasIndexedArchivesEntries(6000, 8002, 7894));
    }

}
