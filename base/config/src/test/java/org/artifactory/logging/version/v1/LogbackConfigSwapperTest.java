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

package org.artifactory.logging.version.v1;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Tests the Logback configuration swapper
 *
 * @author Noam Y. Tenne
 */
@Test
public class LogbackConfigSwapperTest extends XmlConverterTest {

    /**
     * Tests that the swap succeeded and that the new config is up to date
     */
    @Test
    public void testSwap() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v1/logback.xml", new LogbackConfigSwapper());

        Element docRoot = doc.getRootElement();
        Namespace rootNamespace = docRoot.getNamespace();

        @SuppressWarnings({"unchecked"})
        List<Element> appenders = docRoot.getChildren("appender", rootNamespace);

        boolean importExportAppenderExists = false;
        boolean trafficAppenderExists = false;
        boolean requestAppenderExists = false;

        for (Element appender : appenders) {
            Attribute nameAttribute = appender.getAttribute("name", rootNamespace);

            Assert.assertNotNull(nameAttribute, "Found appender null 'name' attribute.");

            if (!importExportAppenderExists && "IMPORT.EXPORT".equals(nameAttribute.getValue())) {
                importExportAppenderExists = true;
                continue;
            }
            if (!trafficAppenderExists && "TRAFFIC".equals(nameAttribute.getValue())) {
                trafficAppenderExists = true;
                continue;
            }
            if (!requestAppenderExists && "REQUEST".equals(nameAttribute.getValue())) {
                requestAppenderExists = true;
            }
        }

        assertTrue(importExportAppenderExists,
                "Import export appender should exist in the logback config after conversion.");
        assertTrue(trafficAppenderExists, "Traffic appender should exist in the logback config after conversion.");
        assertTrue(requestAppenderExists, "Request appender should exist in the logback config after conversion.");
    }
}