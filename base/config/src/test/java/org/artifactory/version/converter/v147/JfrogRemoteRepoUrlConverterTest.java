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
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Test {@link JfrogRemoteRepoUrlConverter}
 *
 * @author Tomer Cohen
 */
@Test
public class JfrogRemoteRepoUrlConverterTest extends XmlConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.1.4.6_wrong_url.xml", new JfrogRemoteRepoUrlConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element repositories = rootElement.getChild("remoteRepositories", namespace);
        List remoteRepos = repositories.getChildren("remoteRepository", namespace);
        for (Object remoteRepo : remoteRepos) {
            Element remoteRepoElement = (Element) remoteRepo;
            Element key = remoteRepoElement.getChild("key", namespace);
            if ("jfrog-libs".equals(key.getText())) {
                Element url = remoteRepoElement.getChild("url", namespace);
                assertEquals("http://repo.jfrog.org/artifactory/libs-releases-local", url.getText());
            } else if ("jfrog-plugins".equals(key.getText())) {
                Element url = remoteRepoElement.getChild("url", namespace);
                assertEquals("http://repo.jfrog.org/artifactory/plugins-releases-local", url.getText());
            }
        }
    }
}
