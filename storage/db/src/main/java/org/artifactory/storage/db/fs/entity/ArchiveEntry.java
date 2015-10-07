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

package org.artifactory.storage.db.fs.entity;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a resolved archive entry (the result of a join).
 *
 * @author Yossi Shaul
 */
public class ArchiveEntry {

    /**
     * The sha1 checksum of the archive containing this entry.
     */
    private final String archiveSha1;

    /**
     * The path of the entry (excluding the name). Can be empty string if the entry is under the root path.
     */
    private final String entryPath;

    /**
     * The name of the entry.
     */
    private final String entryName;

    public ArchiveEntry(String archiveSha1, String entryPath, String entryName) {
        if (StringUtils.isBlank(archiveSha1)) {
            throw new IllegalArgumentException("Archive sha1 cannot be empty");
        }

        if (StringUtils.isBlank(entryName)) {
            throw new IllegalArgumentException("Entry name cannot be empty");
        }

        this.archiveSha1 = archiveSha1;
        this.entryPath = StringUtils.trimToEmpty(entryPath);
        this.entryName = entryName;
    }

    public String getArchiveSha1() {
        return archiveSha1;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public String getEntryName() {
        return entryName;
    }

    /**
     * Returns the path of this entry including the name (path/name)
     * <p><pre>
     * path = "path", name = "name" returns "path/name"
     * path = "", name = "name" returns "name"
     * path = "", name = "" returns ""
     * </pre>
     *
     * @return Returns the path of the entry including the name (path/name)
     */
    public String getPathName() {
        if (StringUtils.isBlank(entryPath)) {
            return entryName;
        } else {
            return entryPath + "/" + entryName;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArchiveEntry that = (ArchiveEntry) o;

        if (!archiveSha1.equals(that.archiveSha1)) {
            return false;
        }
        if (!entryName.equals(that.entryName)) {
            return false;
        }
        if (!entryPath.equals(that.entryPath)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = archiveSha1.hashCode();
        result = 31 * result + entryPath.hashCode();
        result = 31 * result + entryName.hashCode();
        return result;
    }

}
