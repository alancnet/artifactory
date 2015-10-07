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

package org.artifactory.version.converter.v141;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Tomer Cohen
 */
public class ProxyDefaultConverterTest extends XmlConverterTest {

    @Test
    public void covertRemoteRepositoriesWithDefaultProxy() throws Exception {
        Document doc = convertXml("/config/test/config.1.4.0_default-proxy.xml",
                new ProxyDefaultConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element proxies = root.getChild("proxies", ns);
        List proxy = proxies.getChildren();
        Element defaultProxy = (Element) proxy.get(0);
        Element isDefaultProxy = defaultProxy.getChild("defaultProxy", ns);
        Assert.assertNotNull(isDefaultProxy);
        Assert.assertEquals(isDefaultProxy.getText(), "true");
    }

}
