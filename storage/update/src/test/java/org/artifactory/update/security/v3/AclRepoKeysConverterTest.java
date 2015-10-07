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

package org.artifactory.update.security.v3;

import org.artifactory.update.security.SecurityConverterTest;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the conversion of acl repo key.
 *
 * @author Yossi Shaul
 */
@Test
public class AclRepoKeysConverterTest extends SecurityConverterTest {
    private static final Logger log = LoggerFactory.getLogger(AclRepoKeysConverterTest.class);

    @SuppressWarnings({"unchecked"})
    public void convertValidFile() throws Exception {
        String fileMetadata = "/security/v3/security-repokey-convert.xml";
        Document doc = convertXml(fileMetadata, new AclRepoKeysConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();
        Element aclsElement = root.getChild("acls");
        List<Element> acls = aclsElement.getChildren();
        assertEquals(acls.size(), 3, "Expecting 3 acls");

        Element permissionTarget = acls.get(0).getChild("permissionTarget");
        assertNull(permissionTarget.getChild("string"), "repoKey should have been removed");
        Element repoKeysElement = permissionTarget.getChild("repoKeys");
        assertNotNull(repoKeysElement, "repoKeys element wasn't created");
        List<Element> repoKeys = repoKeysElement.getChildren();
        assertEquals(repoKeys.size(), 1, "Only one repo key expected");
        assertEquals(repoKeys.get(0).getText(), "ANY", "Unexpected repo key");

        permissionTarget = acls.get(1).getChild("permissionTarget");
        assertNull(permissionTarget.getChild("string"), "repoKey should have been removed");
        repoKeysElement = permissionTarget.getChild("repoKeys");
        assertNotNull(repoKeysElement, "repoKeys element wasn't created");
        repoKeys = repoKeysElement.getChildren();
        assertEquals(repoKeys.size(), 1, "Only one repo key expected");
        assertEquals(repoKeys.get(0).getText(), "ext-releases-local", "Unexpected repo key");
    }
}
