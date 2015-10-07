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

package org.artifactory.update.md.v130beta3;

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
 * Tests the file metadata converter from version 1.3.0beta3 to 1.3.0beta6.
 *
 * @author Yossi Shaul
 */
@Test
public class ArtifactoryFolderConverterTest extends MetadataConverterTest {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryFolderConverterTest.class);

    public void convertValidFile() throws Exception {
        String fileMetadata = "/metadata/v130beta3/artifactory.folder.xml";
        Document doc = convertXml(fileMetadata, new ArtifactoryFolderConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();
        assertEquals(root.getName(), "artifactory-folder", "Root node should have been renamed");
        Element repoPath = root.getChild("repoPath");
        assertNotNull(repoPath, "Converter should create repoPath node");
        assertEquals(repoPath.getChildren().size(), 2, "Repo path should contains repoKey and path");

        Element extension = root.getChild("extension");
        assertNotNull(repoPath, "Converter should create extension node");
        assertEquals(extension.getChildText("modifiedBy"), "anonymous",
                "Modified by should be under the extension");
    }

}