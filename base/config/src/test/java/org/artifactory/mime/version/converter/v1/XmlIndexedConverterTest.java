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

package org.artifactory.mime.version.converter.v1;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Tests {@link XmlIndexedConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class XmlIndexedConverterTest extends MimeTypeConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v1.xml", new XmlIndexedConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List mimetypes = rootElement.getChildren("mimetype", namespace);
        // make sure there are no more 'xml' attributes
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            Attribute empty = mimeTypeElement.getAttribute("xml", namespace);
            assertNull(empty, "'xml' attribute should have been replaced with 'index'");
        }

        // check specific mime types
        Element applicationXml = getType(mimetypes, namespace, "application/xml");
        assertEquals("false", applicationXml.getAttributeValue("index"));

        Element pom = getType(mimetypes, namespace, "application/x-maven-pom+xml");
        assertEquals("true", pom.getAttributeValue("index"));

        Element ivy = getType(mimetypes, namespace, "application/x-ivy+xml");
        assertEquals("true", ivy.getAttributeValue("index"));
    }
}
