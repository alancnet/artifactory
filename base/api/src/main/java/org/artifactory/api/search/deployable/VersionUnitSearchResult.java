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

package org.artifactory.api.search.deployable;

import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.search.SearchResultBase;

/**
 * Holds version unit search result data.
 *
 * @author Noam Y. Tenne
 */
public class VersionUnitSearchResult extends SearchResultBase {

    private VersionUnit versionUnit;

    /**
     * Main constructor
     *
     * @param versionUnit Version unit
     */
    public VersionUnitSearchResult(VersionUnit versionUnit) {
        super(null);
        this.versionUnit = versionUnit;
    }

    /**
     * Returns the version unit
     *
     * @return Version unit
     */
    public VersionUnit getVersionUnit() {
        return versionUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        VersionUnitSearchResult that = (VersionUnitSearchResult) o;

        if (!versionUnit.equals(that.versionUnit)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + versionUnit.hashCode();
        return result;
    }
}
