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

package org.artifactory.api.search.archive;

import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.fs.ItemInfo;

/**
 * Holds archive content search result data. Inherits from SearchResult and adds two fields: -Entry name -Entry path
 *
 * @author Noam Tenne
 */
public class ArchiveSearchResult extends ArtifactSearchResult {

    private final String entryName;
    private final String entryPath;
    private final boolean isRealEntry;

    /**
     * @param itemInfo    Item info the info of the zip or jar containing this entry
     * @param entryName
     * @param entryPath   Entry path the full path of the entry with the file name at the end
     * @param isRealEntry Whether the entry is a real one, or just an informative missing entry message
     */
    public ArchiveSearchResult(ItemInfo itemInfo, String entryName, String entryPath, boolean isRealEntry) {
        super(itemInfo);
        this.entryName = entryName;
        this.entryPath = entryPath;
        this.isRealEntry = isRealEntry;
    }

    // Used by Wicket
    @SuppressWarnings("UnusedDeclaration")
    public String getEntryName() {
        return entryName;
    }

    // Used by Wicket
    @SuppressWarnings("UnusedDeclaration")
    public String getLowerCaseEntryName() {
        return entryName.toLowerCase();
    }

    /**
     * Returns the entry full path
     *
     * @return String - entry full path
     */
    public String getEntryPath() {
        return entryPath;
    }

    public boolean isRealEntry() {
        return isRealEntry;
    }
}