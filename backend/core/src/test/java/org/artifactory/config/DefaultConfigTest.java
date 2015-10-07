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

package org.artifactory.config;

import org.apache.commons.io.IOUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.reader.CentralConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Tests the default configuration works.
 *
 * @author Yossi Shaul
 */
@Test
public class DefaultConfigTest {
    private static final Logger log = LoggerFactory.getLogger(DefaultConfigTest.class);

    public void testReadDefaultConfig() throws IOException {
        InputStream is = getClass().getResourceAsStream("/META-INF/default/artifactory.config.xml");
        File confFile = File.createTempFile("ConfTests", null);
        confFile.deleteOnExit();
        OutputStream os = new PrintStream(confFile);
        IOUtils.copy(is, os);
        is.close();
        os.close();
        CentralConfigDescriptor centralConfig =
                new CentralConfigReader().read(confFile);
        log.debug("config = " + centralConfig);
    }
}
