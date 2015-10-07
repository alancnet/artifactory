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

package org.artifactory.mime.version.converter;

import org.artifactory.convert.XmlConverterTest;
import org.artifactory.mime.version.MimeTypesVersion;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests the {@link LatestVersionConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class LatestVersionConverterTest extends XmlConverterTest {
    public void convert() throws Exception {
        Document document = convertXml("/org/artifactory/mime/version/mimetypes-v1.xml", new LatestVersionConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        String version = rootElement.getAttributeValue("version", namespace);
        String latestVersion = MimeTypesVersion.getCurrent().versionString();
        assertEquals(version, latestVersion, "Expected converter to change version to the latest");
    }

}
