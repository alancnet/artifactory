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

import org.artifactory.checksum.ChecksumType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link org.artifactory.api.search.artifact.ChecksumSearchControls}.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumSearchControlsTest {

    public void isWildcardsOnly() throws Exception {
        ChecksumSearchControls controls = new ChecksumSearchControls();
        assertTrue(controls.isWildcardsOnly());
        controls.addChecksum(ChecksumType.sha1, "*");
        assertTrue(controls.isWildcardsOnly());
        controls.getChecksums().remove(ChecksumType.md5);
        controls.addChecksum(ChecksumType.sha1, "999");
        assertFalse(controls.isWildcardsOnly());
    }
}
