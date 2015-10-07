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

package org.artifactory.model.xstream.fs;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Simple serializable object with zip entry information.
 *
 * @author Chen Keinan
 */
public class ArchiveEntryImpl implements Serializable, ZipEntryInfo {
    final private String path;    // full path of the entry
    final private String name;    // entry name
    final private long time;    // modification time (in DOS time)
    private long crc = 0;        // crc-32 of entry data
    final private long size;    // uncompressed size of entry data
    private long compressedSize = 0;   // compressed size of entry data
    private String comment = null;        // optional comment string for entry
    final private boolean directory;    // is this entry a directory

    /**
     * Builds a directory entry with just a name (some jar files doesn't contain ZipEntry for directories).
     *
     * @param path      The full path of the entry
     * @param directory
     */
    public ArchiveEntryImpl(@Nonnull String path, boolean directory) {
        this.path = path;
        this.name = PathUtils.getFileName(path);
        this.time = 0;
        this.size = 0;
        this.compressedSize = 0;
        this.comment = null;
        this.crc = 0;
        this.directory = directory;
    }

    public ArchiveEntryImpl(@Nonnull ArchiveEntry... entries) {
        if (entries.length == 0) {
            throw new IllegalArgumentException("Cannot create ZipEntryInfo without a ZipEntry!");
        }

        ArchiveEntry entry = entries[entries.length - 1];

        if (entries.length > 1) {
            StringBuilder fullPath = new StringBuilder();
            for (int i = 0; i < entries.length; i++) {
                fullPath.append(entries[i].getName());
                if (i != entries.length - 1) {
                    fullPath.append(RepoPath.ARCHIVE_SEP).append('/');
                }
            }
            this.path = fullPath.toString();
        } else {
            this.path = entry.getName();
        }
        this.name = PathUtils.getFileName(entry.getName());
        this.time = entry.getLastModifiedDate().getTime();
        this.size = entry.getSize();
        this.directory = entry.isDirectory();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public long getCrc() {
        return crc;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public long getCompressedSize() {
        return compressedSize;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArchiveEntryImpl zipEntry = (ArchiveEntryImpl) o;

        if (directory != zipEntry.directory) {
            return false;
        }
        if (!path.equals(zipEntry.path)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + (directory ? 1 : 0);
        return result;
    }
}
