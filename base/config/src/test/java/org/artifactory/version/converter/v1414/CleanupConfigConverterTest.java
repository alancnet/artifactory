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

import org.apache.commons.lang.StringUtils;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the unused artifacts cleanup converter
 *
 * @author Shay Yaakov
 */
@Test
public class CleanupConfigConverterTest extends XmlConverterTest {

    public void testCreateCleanupCronExp() throws Exception {
        Document document = convertXml("/config/test/config.1.4.9.no.gc.xml", new CleanupConfigConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element cleanupConfigElement = rootElement.getChild("cleanupConfig", namespace);

        assertNotNull(cleanupConfigElement, "Expected to find a cleanup configuration element.");

        String cronExp = cleanupConfigElement.getChildText("cronExp", namespace);
        assertTrue(!StringUtils.isBlank(cronExp), "Expected cron exp field to have a valid value");
    }
}
