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

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the conversion done by {@link org.artifactory.version.converter.v1414.AssumedOfflineConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class AssumedOfflineConverterTest extends XmlConverterTest {

    public void defaultConfigTest() throws Exception {
        Document document = convertXml("/config/install/config.1.4.12.xml", new AssumedOfflineConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List remoteRepos = rootElement.getChild("remoteRepositories", namespace).getChildren();

        for (Object o : remoteRepos) {
            Element remoteRepo = (Element) o;
            assertNull(remoteRepo.getChild("assumedOfflinePeriodSecs", namespace),
                    "Should not add element if failed not exist");
        }
    }

    public void withFailedRetrievalCache() throws Exception {
        Document document = convertXml("/config/test/config.1.4.12_failed_retrieval_cache.xml",
                new AssumedOfflineConverter());
        debugContent(document);
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List remoteRepos = rootElement.getChild("remoteRepositories", namespace).getChildren();

        Element remote1Element = (Element) remoteRepos.get(0);
        assertNull(remote1Element.getChild("assumedOfflinePeriodSecs", namespace),
                "Should not add element if failed not exist");

        Element remote2Element = (Element) remoteRepos.get(1);
        assertNull(remote2Element.getChild("failedRetrievalCachePeriodSecs"), "Old element name should not exist");
        Element assumedOfflinePeriod = remote2Element.getChild("assumedOfflinePeriodSecs", namespace);
        assertNotNull(assumedOfflinePeriod, "Expected to find the converted element.");
        assertEquals(assumedOfflinePeriod.getValue(), "300", "Unexpected default value");
    }
}
