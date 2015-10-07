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

package org.artifactory.version;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Base class for tests shared by all SubConfigElementVersions.
 *
 * @author Yossi Shaul
 */
public abstract class VersionTest {

    @Test
    public void versionsCoverage() {
        // Check that all Artifactory versions are covered by a DB version
        SubConfigElementVersion[] versions = getVersions();
        Assert.assertTrue(versions.length > 0);
        assertEquals(versions[0].getComparator().getFrom(), getFirstSupportedArtifactoryVersion(),
                "First version should start at first supported Artifactory version");
        assertEquals(versions[versions.length - 1].getComparator().getUntil(), ArtifactoryVersion.getCurrent(),
                "Last version should be the current one");
        for (int i = 0; i < versions.length; i++) {
            SubConfigElementVersion version = versions[i];
            if (i + 1 < versions.length) {
                assertEquals(version.getComparator().getUntil().ordinal(),
                        versions[i + 1].getComparator().getFrom().ordinal() - 1,
                        "Versions should have full coverage and leave no holes in the list of Artifactory versions");
            }
        }
    }

    protected ArtifactoryVersion getFirstSupportedArtifactoryVersion() {
        return ArtifactoryVersion.v122rc0;
    }

    protected abstract SubConfigElementVersion[] getVersions();
}
