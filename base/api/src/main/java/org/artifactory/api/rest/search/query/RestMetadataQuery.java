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
public class RestMetadataQuery extends BaseRestQuery {

    private boolean xmlSearch;
    private String metadataName;
    private String path;
    private String value;
    private boolean exactMatch;

    /**
     * Default constructor
     */
    public RestMetadataQuery() {
    }

    /**
     * Indicates if the search should be performed as an XML search or as a metadata search
     *
     * @return True if the search should be performed as an XML search. False if as a metadata search
     */
    public boolean isXmlSearch() {
        return xmlSearch;
    }

    /**
     * Sets if the search should be performed as an XML search or as a metadata search
     *
     * @param xmlSearch True if the search should be performed as an XML search. False if as a metadata search
     */
    public void setXmlSearch(boolean xmlSearch) {
        this.xmlSearch = xmlSearch;
    }

    /**
     * Returns the name of the metadata type to search for
     *
     * @return Metadata type name to search for
     */
    public String getMetadataName() {
        return metadataName;
    }

    /**
     * Sets the name of the metadata type to search for
     *
     * @param metadataName Metadata type name to search for
     */
    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    /**
     * Returns the path to search for within the metadata
     *
     * @return The path to search for within the metadata
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path to search for within the metadata
     *
     * @param path The path to search for within the metadata
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the value to search for within the metadata
     *
     * @return The value to search for within the metadata
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value to search for within the metadata
     *
     * @param value The value to search for within the metadata
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Indicates if the results should be an exact match of the value
     *
     * @return True if the results should be an exact match of the value
     */
    public boolean isExactMatch() {
        return exactMatch;
    }

    /**
     * Sets if the results should be an exact match of the value
     *
     * @param exactMatch True if the results should be an exact match of the value
     */
    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }
}
