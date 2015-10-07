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

package org.artifactory.util;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.version.ArtifactoryVersioning;
import org.artifactory.version.VersionParser;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Tests the behaviour of the Version Parser
 *
 * @author Noam Tenne
 */
public class VersionParserTest {

    /**
     * Supplies the version parser with a mock versioning xml file and tries to parse ot to an ArtifactoryVersioning
     * object
     *
     * @throws IOException - Exception thrown if there are any problems with reading from the stream
     */
    @Test
    public void testLoadResource() throws IOException {
        InputStream is = getClass().getResourceAsStream("/org/artifactory/util/versioning.xml");
        String input = IOUtils.toString(is, "utf-8");
        ArtifactoryVersioning artifactoryVersioning = VersionParser.parse(input);
        artifactoryVersioning.getLatest();
    }
}