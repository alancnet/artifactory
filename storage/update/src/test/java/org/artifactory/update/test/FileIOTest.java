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

package org.artifactory.update.test;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

/**
 * User: freds Date: Jun 11, 2008 Time: 3:26:22 PM
 */
@Test
public class FileIOTest {
    public void testFileReturns() throws Exception {
        String tmpFileName = System.getProperty("java.io.tmpdir");
        assertNotNull(tmpFileName);
        File tmpFolder = new File(tmpFileName);
        assertTrue(tmpFolder.exists());
        File testFolder = new File(tmpFolder, "FolderTest-" + System.currentTimeMillis());
        testFolder.deleteOnExit();
        assertFalse(testFolder.exists());
        assertTrue(testFolder.mkdir());
        assertTrue(testFolder.exists());
        // Returns false if it exists
        assertFalse(testFolder.mkdir());
    }
}
