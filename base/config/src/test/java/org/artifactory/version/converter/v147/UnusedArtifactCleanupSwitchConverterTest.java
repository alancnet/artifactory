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

package org.artifactory.version.converter.v147;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
@Test
public class UnusedArtifactCleanupSwitchConverterTest extends XmlConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.1.4.6_wrong_url.xml",
                new UnusedArtifactCleanupSwitchConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element remoteRepositoriesElement = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepositoriesElement != null) {
            List<Element> remoteRepositoryElements =
                    remoteRepositoriesElement.getChildren("remoteRepository", namespace);
            if (remoteRepositoryElements != null && !remoteRepositoryElements.isEmpty()) {

                for (Element remoteRepositoryElement : remoteRepositoryElements) {

                    Assert.assertNull(remoteRepositoryElement.getChild("unusedArtifactsCleanupEnabled", namespace),
                            "The 'unusedArtifactsCleanupEnabled' field was not removed from: " +
                                    remoteRepositoryElement.getChild("key", namespace).getText());
                }
            }
        }
    }
}