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

import org.artifactory.update.security.SecurityConverterTest;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests the UserPermissionsConverter.
 *
 * @author Yossi Shaul
 */
public class UserPermissionsConverterTest extends SecurityConverterTest {
    private static final Logger log = LoggerFactory.getLogger(UserPermissionsConverterTest.class);

    @Test
    public void convertOutputOfSimpleUserConverter() throws Exception {
        String fileMetadata = "/security/v1/security.xml";
        Document doc = convertXml(fileMetadata, new UserPermissionsConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();

        Element usersElement = root.getChild("users");
        List users = usersElement.getChildren();
        Element admin = (Element) users.get(0);
        assertEquals(admin.getChildText("username"), "admin");
        assertEquals(admin.getChildText("admin"), "true");

        Element user1 = (Element) users.get(1);
        assertNull(user1.getChild("admin"));

        Element user2 = (Element) users.get(2);
        assertNull(user2.getChildText("admin"));

    }
}
