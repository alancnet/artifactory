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

package org.artifactory.storage.build.service;

/**
 * Date: 11/27/12
 * Time: 10:15 AM
 *
 * @author freds
 */
public enum BuildSearchCriteria {
    IN_ARTIFACTS, IN_DEPENDENCIES, IN_BOTH;

    public final boolean searchInArtifacts() {
        return this == IN_ARTIFACTS || this == IN_BOTH;
    }

    public final boolean searchInDependencies() {
        return this == IN_DEPENDENCIES || this == IN_BOTH;
    }
}
