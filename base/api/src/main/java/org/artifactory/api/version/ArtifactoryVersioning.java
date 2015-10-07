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

package org.artifactory.api.version;

/**
 * An object that is used to contain the VersionHolders of the the VersionInfoService
 *
 * @author Noam Tenne
 */
public class ArtifactoryVersioning {
    /**
     * A version holder for the latest version of any kind (beta, rc, release)
     */
    private VersionHolder latest;
    /**
     * A version holder for the latest release version
     */
    private VersionHolder release;

    /**
     * Main constructor
     *
     * @param latest  Version holder with latest version of any kind
     * @param release Version holder with latest release version
     */
    public ArtifactoryVersioning(VersionHolder latest, VersionHolder release) {
        this.latest = latest;
        this.release = release;
    }

    /**
     * Returns the version holder with latest version of any kind
     *
     * @return VersionHolder - Latest version of any kind
     */
    public VersionHolder getLatest() {
        return latest;
    }

    /**
     * Returns the version holder with latest release version
     *
     * @return VersionHolder - Latest release version
     */
    public VersionHolder getRelease() {
        return release;
    }
}