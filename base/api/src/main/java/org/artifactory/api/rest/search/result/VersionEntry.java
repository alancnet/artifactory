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

package org.artifactory.api.rest.search.result;

/**
 * Compound result object to be returned by REST artifact versions searches
 *
 * @author Shay Yaakov
 */
public class VersionEntry {

    /**
     * The version string
     */
    private String version;
    /**
     * True if it represents an integration (snapshot) version in the repository it originated
     */
    private boolean integration;

    /**
     * Default constructor for Json parsing
     */
    public VersionEntry() {

    }

    public VersionEntry(String version, boolean integration) {
        this.version = version;
        this.integration = integration;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isIntegration() {
        return integration;
    }

    public void setIntegration(boolean integration) {
        this.integration = integration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionEntry that = (VersionEntry) o;

        if (integration != that.integration) {
            return false;
        }

        if (!version.equals(that.version)) {
            return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + (integration ? 1 : 0);
        return result;
    }
}