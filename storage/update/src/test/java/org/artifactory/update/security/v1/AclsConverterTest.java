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

package org.artifactory.update.security.v1;

import org.artifactory.update.security.v2.SimpleUserConverterTest;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests conversion done by RepoPathAclConverter.
 *
 * @author Yossi Shaul
 */
public class AclsConverterTest extends SimpleUserConverterTest {
    private static final Logger log = LoggerFactory.getLogger(AclsConverterTest.class);

    @Test
    public void convertOutputOfSimpleUserConverter() throws Exception {
        String fileMetadata = "/security/v1/security.xml";
        Document doc = convertXml(fileMetadata, new AclsConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();

        assertNull(root.getChild("repoPaths"), "repoPaths element should be removed");

        Element aclsElement = root.getChild("acls");
        List<Element> acls = aclsElement.getChildren();
        assertEquals(acls.size(), 2, "Expecting two acls");

        Element acl1 = acls.get(0);
        assertEquals(acl1.getName(), "org.artifactory.security.RepoPathAcl", "ACL element not renamed");
        assertNull(acl1.getChildText("aclObjectIdentity"), "aclObjectIdentity should have been replaced");
        assertEquals(acl1.getChildText("identifier"), "ANY%3aANY", "Acl identifier mismatch");
        Element acesElement = acl1.getChild("aces");
        assertNotNull(acesElement, "aces element shouldn't be null");
        Element listElement = acesElement.getChild("list");
        assertNotNull(listElement, "List element should not be null");
        List aces = listElement.getChildren("org.artifactory.security.RepoPathAce");
        assertEquals(aces.size(), 1, "Expecting one ace");
        Element pathAce = (Element) aces.get(0);
        assertEquals(pathAce.getChildText("principal"), "anonymous", "Expected anonymous user");
        assertEquals(pathAce.getChildText("mask"), "1", "Expected mask of 1");

        Element acl2 = acls.get(1);
        assertEquals(acl2.getName(), "org.artifactory.security.RepoPathAcl", "ACL element not renamed");
        assertNull(acl2.getChildText("aclObjectIdentity"), "aclObjectIdentity should have been replaced");
        assertEquals(acl2.getChildText("identifier"), "libs-releases%3aorg.apache", "Acl identifier mismatch");
        Element aces2Element = acl2.getChild("aces");
        assertNotNull(aces2Element, "aces element shouldn't be null");
        listElement = aces2Element.getChild("list");
        assertNotNull(listElement, "List element should not be null");
        aces = listElement.getChildren("org.artifactory.security.RepoPathAce");
        assertEquals(aces.size(), 2, "Expecting one ace");
        pathAce = (Element) aces.get(0);
        assertEquals(pathAce.getChildText("principal"), "momo", "Unexpected user");
        assertEquals(pathAce.getChildText("mask"), "7", "Unexpected mask");
        pathAce = (Element) aces.get(1);
        assertEquals(pathAce.getChildText("principal"), "yossis", "Unexpected user");
        assertEquals(pathAce.getChildText("mask"), "6", "Unexpected mask");
    }
}