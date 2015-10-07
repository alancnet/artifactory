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

package org.artifactory.update.md.v230;

import org.artifactory.fs.ItemInfo;
import org.artifactory.update.md.MetadataConverterTest;
import org.artifactory.util.ResourceUtils;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Noam Y. Tenne
 */
@Test
public class RepoPathClassConverterTests extends MetadataConverterTest {
    private static final Logger log = LoggerFactory.getLogger(RepoPathClassConverterTests.class);

    public void testInfo() throws Exception {
        convertItemInfo("folder", new BaseRepoPathClassConverter.FolderRepoPathClassConverter());
        convertItemInfo("file", new BaseRepoPathClassConverter.FileRepoPathClassConverter());
    }

    private void convertItemInfo(String type, BaseRepoPathClassConverter requiredConverter) throws Exception {
        Document doc = convertXml("/metadata/v230/artifactory-" + type + ".xml", requiredConverter);

        String result = XmlUtils.outputString(doc);
        log.debug(result);

        ItemInfo itemInfo = (ItemInfo) xstream.fromXML(result);
        Assert.assertNotNull(itemInfo.getModifiedBy());

        ItemInfo expected = (ItemInfo) xstream.fromXML(
                ResourceUtils.getResource("/metadata/v230/artifactory-" + type + "-expected.xml"));
        Assert.assertTrue(itemInfo.isIdentical(expected));
    }
}
