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

package org.artifactory.descriptor.repo;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sorts local repo descriptors in an alphabetical order with ordinary locals preceding caches
 *
 * @author Noam Y. Tenne
 */
public class LocalRepoAlphaComparator implements Comparator<LocalRepoDescriptor>, Serializable {
    @Override
    public int compare(LocalRepoDescriptor repo1, LocalRepoDescriptor repo2) {
        boolean repo1IsCache = repo1.isCache();
        boolean repo2IsCache = repo2.isCache();

        if (repo1IsCache && !repo2IsCache) {
            return 1;
        } else if (!repo1IsCache && repo2IsCache) {
            return -1;
        }
        return repo1.getKey().compareTo(repo2.getKey());
    }
}