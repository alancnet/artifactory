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
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.model.xstream.fs.ZipEntryImpl;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.dao.ArchiveEntriesDao;
import org.artifactory.storage.db.fs.entity.ArchiveEntry;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.service.ArchiveEntriesService;
import org.artifactory.util.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Set;

/**
 * A business service to interact with the archive entries table.
 *
 * @author Yossi Shaul
 */
@Service
public class ArchiveEntriesServiceImpl implements ArchiveEntriesService {

    @Autowired
    private DbService dbService;

    @Autowired
    private ArchiveEntriesDao archiveEntriesDao;

    @Override
    public boolean isIndexed(String archiveSha1) {
        try {
            return archiveEntriesDao.isIndexed(archiveSha1);
        } catch (SQLException e) {
            throw new VfsException("Failed to check indexed entries for '" + archiveSha1 + "'", e);
        }
    }

    @Override
    @Nonnull
    public Set<ZipEntryInfo> getArchiveEntries(String archiveSha1) {
        try {
            Set<ArchiveEntry> archiveEntries = archiveEntriesDao.loadByChecksum(archiveSha1);
            Set<ZipEntryInfo> entries = Sets.newLinkedHashSet();
            for (ArchiveEntry entry : archiveEntries) {
                entries.add(new ZipEntryImpl(entry.getPathName(), false));
            }
            return entries;
        } catch (SQLException e) {
            throw new StorageException("Failed to load archive entries for " + archiveSha1, e);
        }
    }

    @Override
    public void addArchiveEntries(String archiveSha1, Set<? extends ZipEntryInfo> entries) {
        // insert main entry to the indexed_archives table
        try {
            // create indexed_archives row
            long indexedArchiveId = dbService.nextId();
            if (!archiveEntriesDao.createIndexedArchive(archiveSha1, indexedArchiveId)) {
                throw new StorageException("Failed to insert indexed archive entry for " + archiveSha1);
            }

            for (ZipEntryInfo zipEntry : entries) {
                // for each entry add one new row to the many to many and to the entries path
                ArchiveEntry archiveEntry = zipEntryInfoToArchiveEntry(archiveSha1, zipEntry);

                // select or create id from archive path
                long archivePathId = archiveEntriesDao.findArchivePathId(archiveEntry.getEntryPath());
                if (archivePathId == DbService.NO_DB_ID) {
                    archivePathId = dbService.nextId();
                    if (!archiveEntriesDao.createArchivePath(archivePathId, archiveEntry.getEntryPath())) {
                        throw new StorageException("Failed to insert archive path: " + archiveEntry.getEntryPath());
                    }
                }

                // select or create id from archive name
                long archiveNameId = archiveEntriesDao.findArchiveNameId(archiveEntry.getEntryName());
                if (archiveNameId == DbService.NO_DB_ID) {
                    archiveNameId = dbService.nextId();
                    if (!archiveEntriesDao.createArchiveName(archiveNameId, archiveEntry.getEntryName())) {
                        throw new StorageException("Failed to insert archive name: " + archiveEntry.getEntryName());
                    }
                }

                // before inserting to the many to many relation check that such entry doesn't entry doesn't exist
                // this might happen for example in case insensitive databases if the archive contains two entries
                // with difference only is character casing
                if (!archiveEntriesDao.hasIndexedArchivesEntries(indexedArchiveId, archivePathId, archiveNameId)) {
                    archiveEntriesDao.createIndexedArchivesEntries(indexedArchiveId, archivePathId, archiveNameId);
                }
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to insert archive entries: " + e.getMessage(), e);
        }
    }

    private ArchiveEntry zipEntryInfoToArchiveEntry(String archiveSha1, ZipEntryInfo entry) {
        String path = PathUtils.getParent(entry.getPath());
        return new ArchiveEntry(archiveSha1, path, entry.getName());
    }

    @Override
    public boolean deleteArchiveEntries(String archiveSha1) {
        try {
            return archiveEntriesDao.deleteByChecksum(archiveSha1) > 0;
        } catch (SQLException e) {
            throw new VfsException("Failed to delete indexed entries for '" + archiveSha1 + "'", e);
        }
    }

    @Override
    public int deleteUnusedPathIds() {
        try {
            return archiveEntriesDao.deleteUnusedPathIds();
        } catch (SQLException e) {
            throw new StorageException("Failed to delete unused path ids", e);
        }
    }

    @Override
    public int deleteUnusedNameIds() {
        try {
            return archiveEntriesDao.deleteUnusedNameIds();
        } catch (SQLException e) {
            throw new StorageException("Failed to delete unused path ids", e);
        }
    }
}
