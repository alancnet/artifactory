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

package org.artifactory.update.md.v130beta6;

import org.artifactory.fs.FolderInfo;
import org.artifactory.update.md.MetadataConverterTest;
import org.artifactory.util.ResourceUtils;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the conversion of folder info metadata.
 *
 * @author Yossi Shaul
 */
@Test
public class FolderAdditionalInfoNameConverterTest extends MetadataConverterTest {
    private static final Logger log = LoggerFactory.getLogger(FolderAdditionalInfoNameConverterTest.class);

    public void convertValidFolderInfo() throws Exception {
        String fileMetadata = "/metadata/v130beta6/artifactory-folder.xml";
        Document doc = convertXml(fileMetadata, new FolderAdditionalInfoNameConverter());

        String result = XmlUtils.outputString(doc);
        log.debug(result);

        // the result is intermediate so it might not be compatible with latest FolderInfo
        // but for now it is a good test to test the resulting FolderInfo
        FolderInfo folderInfo = (FolderInfo) xstream.fromXML(result);
        Assert.assertNotNull(folderInfo.getModifiedBy());

        FolderInfo expected = (FolderInfo) xstream.fromXML(
                ResourceUtils.getResource("/metadata/v130beta6/artifactory-folder-expected.xml"));
        Assert.assertTrue(folderInfo.isIdentical(expected));
    }
}
