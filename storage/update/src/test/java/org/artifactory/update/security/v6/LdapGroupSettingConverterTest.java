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

package org.artifactory.update.security.v6;

import org.artifactory.update.security.SecurityConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Test the Ldap Group settings converter
 *
 * @author Tomer Cohen
 */
@Test
public class LdapGroupSettingConverterTest extends SecurityConverterTest {

    public void convertValidFile() throws Exception {
        String fileMetadata = "/security/v6/security.xml";
        Document document = convertXml(fileMetadata, new LdapGroupSettingXmlConverter());
        Element rootElement = document.getRootElement();
        Element child = rootElement.getChild("users");
        List children = child.getChildren("user");
        assertEquals(children.size(), 5, "Expecting 5 users");
        Object user = children.get(4);
        Element userElement = (Element) user;
        Element groups = userElement.getChild("groups");
        List userGroup = groups.getChildren("userGroup");
        assertEquals(userGroup.size(), 2, "Expecting 2 groups");
        Element firstGroup = (Element) userGroup.get(0);
        Assert.assertEquals(firstGroup.getText(), "group1");
        Element secondGroup = (Element) userGroup.get(1);
        Assert.assertEquals(secondGroup.getText(), "group2");
    }

}
