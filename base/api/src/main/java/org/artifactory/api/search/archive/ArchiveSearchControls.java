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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.SearchControlsBase;

/**
 * Holds the archive content search parameters
 *
 * @author Noam Tenne
 */
public class ArchiveSearchControls extends SearchControlsBase {
    private String path;
    private String name;
    private boolean excludeInnerClasses;
    private boolean shouldCalcEntries;
    private boolean searchClassResourcesOnly;

    /**
     * Default constructor
     */
    public ArchiveSearchControls() {
        excludeInnerClasses = false;
        shouldCalcEntries = true;
        searchClassResourcesOnly = false;
    }

    /**
     * Copy constructor
     *
     * @param archiveSearchControls Controls to copy
     */
    public ArchiveSearchControls(ArchiveSearchControls archiveSearchControls) {
        this.path = archiveSearchControls.path;
        this.name = archiveSearchControls.name;
        this.selectedRepoForSearch = archiveSearchControls.selectedRepoForSearch;
        setLimitSearchResults(archiveSearchControls.isLimitSearchResults());
        this.excludeInnerClasses = archiveSearchControls.excludeInnerClasses;
        this.shouldCalcEntries = archiveSearchControls.shouldCalcEntries;
        this.searchClassResourcesOnly = archiveSearchControls.searchClassResourcesOnly;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(path) && StringUtils.isEmpty(name);
    }

    @Override
    public boolean isWildcardsOnly() {
        return isWildcardsOnly(path) && isWildcardsOnly(name);
    }

    public boolean isExcludeInnerClasses() {
        return excludeInnerClasses;
    }

    public void setExcludeInnerClasses(boolean excludeInnerClasses) {
        this.excludeInnerClasses = excludeInnerClasses;
    }

    public boolean shouldCalcEntries() {
        return shouldCalcEntries;
    }

    public void setShouldCalcEntries(boolean shouldCalcEntries) {
        this.shouldCalcEntries = shouldCalcEntries;
    }

    public boolean isSearchClassResourcesOnly() {
        return searchClassResourcesOnly;
    }

    public void setSearchClassResourcesOnly(boolean searchClassResourcesOnly) {
        this.searchClassResourcesOnly = searchClassResourcesOnly;
    }

    public String getQueryDisplay() {
        return (StringUtils.isNotEmpty(getPath()) && !isWildcardsOnly(getPath()) ? getPath() + "/" : "") +
                (StringUtils.isNotEmpty(getName()) ? getName() : "");
    }
}