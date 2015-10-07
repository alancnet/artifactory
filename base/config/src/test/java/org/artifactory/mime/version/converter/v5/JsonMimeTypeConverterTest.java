/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.mime.version.converter.v5;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests {@link JsonMimeTypeConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class JsonMimeTypeConverterTest extends MimeTypeConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v5.xml", new JsonMimeTypeConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List mimetypes = rootElement.getChildren("mimetype", namespace);
        Element json = getType(mimetypes, namespace, "application/json");
        assertNotNull(json);
        assertTrue(Boolean.parseBoolean(json.getAttributeValue("viewable")));
        assertEquals(json.getAttributeValue("extensions"), "json");
        assertNull(json.getAttributeValue("archive"), "Index attribute should not exist");
        assertNull(json.getAttribute("index"), "Index attribute should not exist");
    }
}
