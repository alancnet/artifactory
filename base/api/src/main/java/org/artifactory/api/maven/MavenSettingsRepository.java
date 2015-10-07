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

package org.artifactory.api.maven;

/**
 * Contains information of a maven settings repository
 *
 * @author Noam Tenne
 */
public class MavenSettingsRepository extends MavenSettingUnit {
    private boolean handlesSnapshots;

    /**
     * Default constructor
     *
     * @param id               Repository ID
     * @param name             Repository name
     * @param handlesSnapshots Handles snapshots
     */
    public MavenSettingsRepository(String id, String name, boolean handlesSnapshots) {
        super(id, name);
        this.handlesSnapshots = handlesSnapshots;
        isValid();
    }


    /**
     * Does the repository handle snapshots
     *
     * @return boolean - Handles snapshots
     */
    public boolean isHandlesSnapshots() {
        return handlesSnapshots;
    }

    @Override
    public void isValid() {
    }
}
