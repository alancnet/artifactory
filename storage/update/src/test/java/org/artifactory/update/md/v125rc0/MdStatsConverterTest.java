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

package org.artifactory.update.md.v125rc0;

import org.artifactory.update.md.MetadataConverterTest;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the stats metadata converter from version 1.3.0beta3 to 1.3.0beta6.
 *
 * @author Yossi Shaul
 */
@Test
public class MdStatsConverterTest extends MetadataConverterTest {
    private static final Logger log = LoggerFactory.getLogger(MdStatsConverterTest.class);

    public void convertValidFile() throws Exception {
        String fileMetadata = "/metadata/v125rc0/commons-cli-1.0.pom.artifactory-metadata";
        Document doc = convertXml(fileMetadata, new MdStatsConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();
        assertEquals(root.getName(), "artifactory.stats", "Root node should have been renamed");
        Element downloadCount = root.getChild("downloadCount");
        assertNotNull(downloadCount, "Converter should create downloadCount node");
        assertEquals(downloadCount.getText(), "99", "Download count should be 1");
    }

}