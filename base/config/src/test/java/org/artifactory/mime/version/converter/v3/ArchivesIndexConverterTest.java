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

package org.artifactory.mime.version.converter.v3;

import org.artifactory.mime.version.converter.MimeTypeConverterTest;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests {@link IndexArchivesMimeTypeConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class ArchivesIndexConverterTest extends MimeTypeConverterTest {
    public void convert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v2.xml", new ArchivesIndexConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List mimetypes = rootElement.getChildren("mimetype", namespace);
        // make sure every archive has the 'index' attribute with value of 'true'
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            String isArchive = mimeTypeElement.getAttributeValue("archive", namespace);
            if ("true".equals(isArchive)) {
                Attribute index = mimeTypeElement.getAttribute("index", namespace);
                assertNotNull(index, "'index' should exist for the archive");
                assertEquals(index.getBooleanValue(), true, "Index property value should be true");
            }
        }
    }
}
