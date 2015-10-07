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

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.version.ArtifactoryVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * User: freds Date: May 30, 2008 Time: 10:22:23 AM
 */
public class ArtifactoryVersionTest {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryVersionTest.class);

    @Test
    public void testBackupPropertyFile() throws Exception {
        ArtifactoryVersion[] versions = ArtifactoryVersion.values();
        for (ArtifactoryVersion version : versions) {
            if (version.isCurrent()) {
                // No test here
                continue;
            }
            String backupVersionFolder = "/backups/" + version.name() + "/";
            InputStream artifactoryPropertyInputStream =
                    getClass().getResourceAsStream(backupVersionFolder +
                            ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
            if (artifactoryPropertyInputStream != null) {
                Properties properties = new Properties();
                properties.load(artifactoryPropertyInputStream);
                artifactoryPropertyInputStream.close();
                String artifactoryVersion =
                        properties.getProperty(ConstantValues.artifactoryVersion.getPropertyName());
                assertEquals(artifactoryVersion, version.getValue(),
                        "Error in version value for " + version);
                int artifactoryRevision =
                        Integer.parseInt(properties.getProperty(
                                ConstantValues.artifactoryRevision.getPropertyName()));
                assertEquals(artifactoryRevision, version.getRevision(),
                        "Error in revision value for " + version);
            } else {
                log.warn("Version " + version + " does not have a backup test folder in " +
                        backupVersionFolder);
            }
        }
    }
}
