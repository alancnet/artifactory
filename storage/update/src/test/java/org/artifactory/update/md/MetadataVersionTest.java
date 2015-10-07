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

package org.artifactory.update.md;

import org.artifactory.util.ResourceUtils;
import org.artifactory.version.SubConfigElementVersion;
import org.artifactory.version.VersionTest;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;

/**
 * @author freds
 * @date Nov 23, 2008
 */
@Test
public class MetadataVersionTest extends VersionTest {

    @Override
    protected SubConfigElementVersion[] getVersions() {
        return MetadataVersion.values();
    }

    public void detectVersion125rc0() {
        File metadataFile = ResourceUtils.getResourceAsFile(
                "/metadata/v125rc0/commons-cli-1.0.pom.artifactory-metadata");
        MetadataVersion version = MetadataVersion.findVersion(metadataFile);
        assertEquals(version, MetadataVersion.v1);
    }

    public void detectVersion30beta3() {
        File metadataDir = ResourceUtils.getResourceAsFile("/metadata/v130beta3/0.1.23.artifactory-metadata");
        MetadataVersion version = MetadataVersion.findVersion(metadataDir);
        assertEquals(version, MetadataVersion.v2);
    }

    public void detectVersion30beta6() {
        File metadataDir = ResourceUtils.getResourceAsFile("/metadata/v130beta6/junit-3.8.1.jar.artifactory-metadata");
        MetadataVersion version = MetadataVersion.findVersion(metadataDir);
        assertEquals(version, MetadataVersion.v3);
    }

    public void detectVersion230() {
        File metadataDir = ResourceUtils.getResourceAsFile(
                "/metadata/v230/aspectjweaver-1.5.3.jar.artifactory-metadata");
        MetadataVersion version = MetadataVersion.findVersion(metadataDir);
        assertEquals(version, MetadataVersion.v5);
    }
}