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

package org.artifactory.api.rest.search.query;

/**
 * REST API archive query object
 *
 * @author Noam Y. Tenne
 */
public class RestArchiveQuery extends BaseRestQuery {

    private String entryName;
    private boolean shouldCalcEntries = true;

    /**
     * Default constructor
     */
    public RestArchiveQuery() {
    }

    /**
     * Returns the name of the entry to search for
     *
     * @return Entry name to search for
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * Sets the name of the entry name to search for
     *
     * @param entryName Entry name to search for
     */
    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    /**
     * Indicates if to perform result entry analysis
     *
     * @return True if should perform result entry analysis
     */
    public boolean isShouldCalcEntries() {
        return shouldCalcEntries;
    }

    /**
     * Sets if to perform result entry analysis
     *
     * @param shouldCalcEntries True if should perform result entry analysis
     */
    public void setShouldCalcEntries(boolean shouldCalcEntries) {
        this.shouldCalcEntries = shouldCalcEntries;
    }
}