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

package org.artifactory.update.security.v2;

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
 * Tests the SimpleUserConverter.
 *
 * @author Yossi Shaul
 */
@Test
public class SimpleUserConverterTest extends SecurityConverterTest {
    private static final Logger log = LoggerFactory.getLogger(SimpleUserConverterTest.class);

    public void convertValidFile() throws Exception {
        String fileMetadata = "/security/v2/security.xml";
        Document doc = convertXml(fileMetadata, new SimpleUserConverter());

        log.debug(XmlUtils.outputString(doc));

        Element root = doc.getRootElement();
        Element usersElement = root.getChild("users");
        List users = usersElement.getChildren();
        assertEquals(users.size(), 4, "Expecting 4 users");
        Element user = (Element) users.get(0);
        assertEquals(user.getName(), "user", "User element name not changed");
        assertNull(user.getChild("authorities"), "Authorities should be removed");
        assertEquals(user.getChildText("username"), "admin", "Username mismatch");
        assertEquals(user.getChildText("admin"), "true", "Should be admin");

        user = (Element) users.get(1);
        assertEquals(user.getName(), "user", "User element name not changed");
        assertNull(user.getChild("authorities"), "Authorities should be removed");
        assertEquals(user.getChildText("username"), "anonymous", "Username mismatch");
        assertEquals(user.getChildText("admin"), "false", "Should not be admin");
    }
}
