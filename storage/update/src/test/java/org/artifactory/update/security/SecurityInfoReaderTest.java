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

package org.artifactory.update.security;

import org.apache.commons.io.FileUtils;
import org.artifactory.security.SecurityInfo;
import org.artifactory.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Collection;

import static org.testng.Assert.*;

/**
 * Tests the {@link SecurityInfoReader}.
 *
 * @author Yossi Shaul
 */
@Test
public class SecurityInfoReaderTest {
    private static final Logger log = LoggerFactory.getLogger(SecurityInfoReaderTest.class);

    @Test(description = "this test will try to read all the security files under the security test directory")
    public void readAllVersions() {
        File securityDirectory = ResourceUtils.getResourceAsFile("/security");
        Collection securityFiles = FileUtils.listFiles(securityDirectory, new String[]{"xml"}, true);
        assertTrue(securityFiles.size() > 5, "Hey, where are all the security test files? Non found under "
                + securityDirectory.getAbsolutePath());

        SecurityInfoReader reader = new SecurityInfoReader();
        for (Object securityFile : securityFiles) {
            File file = (File) securityFile;
            log.debug("Reading security file: {}", file.getAbsolutePath());
            // lets just check that the reader knows how to read this file...
            SecurityInfo securityInfo = reader.read(file);
            assertNotNull(securityInfo, "Null value returned from security reader for file " + file.getAbsolutePath());
        }
    }

    @Test(description = "just another test on version 6 with few assertions")
    public void readVersion5() {
        File securityFile = ResourceUtils.getResourceAsFile("/security/v6/security.xml");
        SecurityInfo securityInfo = new SecurityInfoReader().read(securityFile);
        assertEquals(securityInfo.getUsers().size(), 5, "Wrong users count");
    }

}
