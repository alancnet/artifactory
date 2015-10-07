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

package org.artifactory.version.converter.v1414;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;

/**
 * Tests the conversion done by {@link org.artifactory.version.converter.v1414.ArchiveBrowsingConverter}.
 *
 * @author Shay Yaakov
 */
@Test
public class ArchiveBrowsingConverterTest extends XmlConverterTest {

    public void convertWithArchiveBrowsing() throws Exception {
        Document document = convertXml("/config/test/config.1.4.13_with_archive_browsing.xml",
                new ArchiveBrowsingConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element securityElement = rootElement.getChild("security", namespace);
        assertNull(securityElement.getChild("archiveBrowsingEnabled", namespace),
                "archiveBrowsingEnabled tag should have been removed");
    }

    public void convertDefault() throws Exception {
        // the default doesn't contain archiveBrowsingEnabled tag
        Document document = convertXml("/config/install/config.1.4.3.xml", new ArchiveBrowsingConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element securityElement = rootElement.getChild("security", namespace);
        assertNull(securityElement.getChild("archiveBrowsingEnabled", namespace));
    }
}
