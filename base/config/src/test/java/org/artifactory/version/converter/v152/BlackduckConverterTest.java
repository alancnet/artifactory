/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.version.converter.v152;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Yoav Luft
 */
@Test
public class BlackduckConverterTest extends XmlConverterTest {

    public void addDefaultProxyTest() throws Exception {
        Document document = convertXml("/config/test/config-1.5.2-blackduck_default_proxy.xml",
                new BlackDuckProxyConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element externalProviders = rootElement.getChild("externalProviders", namespace);
        Element blackduckConfig = externalProviders.getChild("blackduck", namespace);
        Assert.assertEquals(blackduckConfig.getChildText("proxyRef", namespace), "Charles",
                "Expected proxyRef element to be \"Charles\"");
    }

    public void dontAddIfNoDefaultProxy() throws Exception {
        Document document = convertXml("/config/test/config-1.5.2-blackduck_no_default_proxy.xml",
                new BlackDuckProxyConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element externalProviders = rootElement.getChild("externalProviders", namespace);
        Element blackduckConfig = externalProviders.getChild("blackduck", namespace);
        Assert.assertNull(blackduckConfig.getChild("proxyRef", namespace), "There should be no proxy configurations");
    }

    /**
     * We expect in this test that no exception will be thrown if the blackduck section is missing
     *
     * @throws Exception
     */
    public void noBlackduckSection() throws Exception {
        Document document = convertXml("/config/test/config-1.5.2-blackduck_no_settings.xml",
                new BlackDuckProxyConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element externalProviders = rootElement.getChild("externalProviders", namespace);
        Element blackduckConfig = externalProviders.getChild("blackduck", namespace);
        Assert.assertNull(blackduckConfig, "Blackduck section should no appear");
    }
}
