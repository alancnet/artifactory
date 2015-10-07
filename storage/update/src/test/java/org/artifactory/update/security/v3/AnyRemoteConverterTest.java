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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the any remote converter.
 *
 * @author Yossi Shaul
 */
@Test
public class AnyRemoteConverterTest extends SecurityConverterTest {
    private static final Logger log = LoggerFactory.getLogger(AnyRemoteConverterTest.class);

    @SuppressWarnings({"unchecked"})
    public void convertFileWithAnonymousRealOnAny() throws Exception {
        String fileMetadata = "/security/v3/security.xml";
        Document doc = convertXml(fileMetadata, new AnyRemoteConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();
        Element aclsElement = root.getChild("acls");
        List<Element> acls = aclsElement.getChildren();
        assertEquals(acls.size(), 5, "Expecting 5 acls (any remote should have been added)");

        Element anyRemoteAcl = acls.get(4);
        Element permissionTarget = anyRemoteAcl.getChild("permissionTarget");
        assertEquals(permissionTarget.getChildText("name"), "Any Remote", "Wrong permission target name");
        assertEquals(permissionTarget.getChildText("repoKey"), "ANY REMOTE", "Wrong repo key");
        assertEquals(permissionTarget.getChild("includes").getChildText("string"), "**", "Wrong include pattern");
        assertNotNull(permissionTarget.getChild("excludes"), "Excludes must not be null");
        Element aces = anyRemoteAcl.getChild("aces");
        assertEquals(aces.getChildren().size(), 1, "Expecting 1 ace (for anonymous only)");
        Element anonAce = (Element) aces.getChildren().get(0);
        assertEquals(anonAce.getChildText("principal"), "anonymous");
        assertEquals(anonAce.getChildText("mask"), "3", "Mask should be 3 (read+write)");
    }

    public void convertFileWithoutAnonymousRealOnAny() throws Exception {
        String fileMetadata = "/security/v3/security-no-anything-permissions.xml";
        Document doc = convertXml(fileMetadata, new AnyRemoteConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();
        Element aclsElement = root.getChild("acls");
        assertEquals(aclsElement.getChildren().size(), 1, "No acl should have been added");
    }
}
