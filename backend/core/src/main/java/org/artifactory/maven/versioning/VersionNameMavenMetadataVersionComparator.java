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

package org.artifactory.maven.versioning;

import org.artifactory.storage.fs.tree.ItemNode;

/**
 * A {@link MavenMetadataVersionComparator} that determines the latest and release versions based on a versions comparator.
 *
 * @author Yossi Shaul
 */
public class VersionNameMavenMetadataVersionComparator implements MavenMetadataVersionComparator {

    private static final VersionNameMavenMetadataVersionComparator instance =
            new VersionNameMavenMetadataVersionComparator();

    /**
     * The more specific, strings only version comparator
     */
    private final MavenVersionComparator comparator = new MavenVersionComparator();

    @Override
    public int compare(ItemNode o1, ItemNode o2) {
        return comparator.compare(o1.getName(), o2.getName());
    }

    public static VersionNameMavenMetadataVersionComparator get() {
        return instance;
    }
}
