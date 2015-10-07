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

package org.artifactory.storage.db.search.model;

import org.artifactory.sapi.search.ArchiveEntryRow;

/**
 * Holds entry names and paths for collection while search archive contents
 *
 * @author Noam Tenne
 */
public class ArchiveSearchEntry implements ArchiveEntryRow {

    private String entryName;
    private String entryPath;

    /**
     * @param entryPath Path of entry without the name
     * @param entryName Name of entry just the file name
     */
    public ArchiveSearchEntry(String entryPath, String entryName) {
        this.entryName = entryName;
        this.entryPath = entryPath;
    }

    @Override
    public String getEntryName() {
        return entryName;
    }

    @Override
    public String getEntryPath() {
        return entryPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArchiveSearchEntry that = (ArchiveSearchEntry) o;

        if (entryName != null ? !entryName.equals(that.entryName) : that.entryName != null) {
            return false;
        }
        if (entryPath != null ? !entryPath.equals(that.entryPath) : that.entryPath != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = entryName != null ? entryName.hashCode() : 0;
        result = 31 * result + (entryPath != null ? entryPath.hashCode() : 0);
        return result;
    }
}
