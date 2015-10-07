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
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.entity.ArchiveEntry;
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
 * A data access table for the archive_entries table.
 *
 * @author Yossi Shaul
 */
@Repository
public class ArchiveEntriesDao extends BaseDao {

    private final StorageProperties storageProps;

    @Autowired
    public ArchiveEntriesDao(JdbcHelper jdbcHelper, StorageProperties storageProps) {
        super(jdbcHelper);
        this.storageProps = storageProps;
    }

    @Nonnull
    public Set<ArchiveEntry> loadByChecksum(String archiveSha1) throws SQLException {
        ResultSet resultSet = null;
        Set<ArchiveEntry> entries = Sets.newLinkedHashSet();
        try {
            resultSet = jdbcHelper.executeSelect("SELECT ia.archive_sha1, ap.entry_path, an.entry_name FROM " +
                    "indexed_archives ia, indexed_archives_entries iae, archive_paths ap , archive_names an " +
                    "WHERE ia.indexed_archives_id=iae.indexed_archives_id " +
                    "AND iae.entry_path_id=ap.path_id " +
                    "AND iae.entry_name_id=an.name_id " +
                    "AND ia.archive_sha1 = ?", archiveSha1);
            while (resultSet.next()) {
                entries.add(entryFromResultSet(resultSet));
            }
            return entries;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean isIndexed(String archiveSha1) throws SQLException {
        int count = jdbcHelper.executeSelectCount(
                "SELECT COUNT(*) FROM indexed_archives WHERE archive_sha1 = ?", archiveSha1);
        return count > 0;
    }

    /**
     * Deletes the checksum entry from the indexed_archives table and from the many-to-many table
     * indexed_archives_entries. Cleanup of additional referenced tables is taken care by the GC.
     *
     * @param sha1 The checksum to cleanup.
     * @return Number of entries deleted from the many to many table
     */
    public int deleteByChecksum(String sha1) throws SQLException {
        long id = findIndexedArchiveIdByChecksum(sha1);
        if (id != DbService.NO_DB_ID) {
            int entriesCount =
                    jdbcHelper.executeUpdate("DELETE FROM indexed_archives_entries WHERE indexed_archives_id = ?", id);
            jdbcHelper.executeUpdate("DELETE FROM indexed_archives WHERE indexed_archives_id = ?", id);
            return entriesCount;
        }
        return 0;
    }

    public long findIndexedArchiveIdByChecksum(String sha1) throws SQLException {
        ResultSet resultSet = null;
        try {
            resultSet = jdbcHelper.executeSelect(
                    "SELECT indexed_archives_id FROM indexed_archives WHERE archive_sha1 = ?", sha1);
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return DbService.NO_DB_ID;
        } finally {
            DbUtils.close(resultSet);
        }
    }

    public boolean createIndexedArchive(String archiveSha1, long indexedArchiveId) throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO indexed_archives VALUES (?, ?)",
                archiveSha1, indexedArchiveId);
        return updateCount > 0;

    }

    public boolean createArchivePath(long archivePathId, String entryPath) throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO archive_paths VALUES (?, ?)",
                archivePathId, dotIfNullOrEmpty(entryPath));
        return updateCount > 0;
    }

    /**
     * @param entryPath The entry path
     * @return Unique id of the entry path if such exists, {@link org.artifactory.storage.db.DbService#NO_DB_ID} otherwise
     */
    public long findArchivePathId(String entryPath) throws SQLException {
        return jdbcHelper.executeSelectLong("SELECT path_id FROM archive_paths WHERE entry_path = ?",
                dotIfNullOrEmpty(entryPath));
    }

    public boolean createArchiveName(long archiveNameId, String entryName) throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO archive_names VALUES (?, ?)",
                archiveNameId, dotIfNullOrEmpty(entryName));
        return updateCount > 0;
    }

    /**
     * @param entryName The entry name
     * @return Unique id of the entry name if such exists, {@link org.artifactory.storage.db.DbService#NO_DB_ID} otherwise
     */
    public long findArchiveNameId(String entryName) throws SQLException {
        return jdbcHelper.executeSelectLong("SELECT name_id FROM archive_names WHERE entry_name = ?",
                dotIfNullOrEmpty(entryName));
    }

    /**
     * Creates a new record in the many-to-many table of archive to entries.
     *
     * @param indexedArchiveId The indexed archive unique id
     * @param archivePathId    The archive entry path unique id
     * @param archiveNameId    The archive entry name unique id
     * @return True if the new entry was inserted
     */
    public boolean createIndexedArchivesEntries(long indexedArchiveId, long archivePathId, long archiveNameId)
            throws SQLException {
        int updateCount = jdbcHelper.executeUpdate("INSERT INTO indexed_archives_entries VALUES (?, ?, ?)",
                indexedArchiveId, archivePathId, archiveNameId);
        return updateCount > 0;
    }

    /**
     * @return True if there's already an entry with the given ids
     */
    public boolean hasIndexedArchivesEntries(long indexedArchiveId, long archivePathId, long archiveNameId)
            throws SQLException {
        int updateCount = jdbcHelper.executeSelectCount("SELECT count(*) FROM indexed_archives_entries " +
                "WHERE indexed_archives_id = ? AND entry_path_id = ? AND entry_name_id = ?",
                indexedArchiveId, archivePathId, archiveNameId);
        return updateCount > 0;
    }

    public int deleteUnusedPathIds() throws SQLException {
        if (!storageProps.isPostgres()) {
            return jdbcHelper.executeUpdate("DELETE FROM archive_paths WHERE path_id NOT IN " +
                    "(SELECT entry_path_id FROM indexed_archives_entries)");
        } else {
            // NOT EXISTS is much faster on PostgreSQL (see RTFACT-6231)
            return jdbcHelper.executeUpdate("DELETE FROM archive_paths WHERE NOT EXISTS " +
                    "(SELECT 1 FROM indexed_archives_entries i WHERE i.entry_path_id = path_id)");
        }
    }

    public int deleteUnusedNameIds() throws SQLException {
        if (!storageProps.isPostgres()) {
            return jdbcHelper.executeUpdate("DELETE FROM archive_names WHERE name_id NOT IN " +
                    "(SELECT entry_name_id FROM indexed_archives_entries)");
        } else {
            // NOT EXISTS is much faster on PostgreSQL (see RTFACT-6231)
            return jdbcHelper.executeUpdate("DELETE FROM archive_names WHERE NOT EXISTS " +
                    "(SELECT 1 FROM indexed_archives_entries i WHERE i.entry_name_id = name_id)");
        }
    }

    private ArchiveEntry entryFromResultSet(ResultSet rs) throws SQLException {
        return new ArchiveEntry(rs.getString(1), emptyIfNullOrDot(rs.getString(2)), emptyIfNullOrDot(rs.getString(3)));
    }
}
