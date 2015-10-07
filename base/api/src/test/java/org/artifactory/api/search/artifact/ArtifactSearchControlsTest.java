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

package org.artifactory.api.search.artifact;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link org.artifactory.api.search.artifact.ArtifactSearchControls}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactSearchControlsTest {

    public void emptyControls() throws Exception {
        ArtifactSearchControls controls = new ArtifactSearchControls();
        assertTrue(controls.isEmpty(), "Expected empty controls");
        controls.setQuery("boo");
        assertFalse(controls.isEmpty(), "Expected non empty controls");
    }

    public void wildcardsOnlyControls() throws Exception {
        ArtifactSearchControls controls = new ArtifactSearchControls();
        assertTrue(controls.isWildcardsOnly());
        controls.setQuery("*");
        assertTrue(controls.isWildcardsOnly());
        controls.setQuery("?");
        assertTrue(controls.isWildcardsOnly());
        controls.setQuery("**");
        assertTrue(controls.isWildcardsOnly());
        controls.setQuery("?*");
        assertTrue(controls.isWildcardsOnly());
        controls.setQuery("a?*");
        assertFalse(controls.isWildcardsOnly());
        controls.setQuery("*b");
        assertFalse(controls.isWildcardsOnly());
    }
}
