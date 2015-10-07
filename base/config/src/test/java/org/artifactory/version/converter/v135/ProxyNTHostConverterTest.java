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

package org.artifactory.version.converter.v135;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the ProxyNTHostConverter.
 *
 * @author Yossi Shaul
 */
@Test
public class ProxyNTHostConverterTest extends XmlConverterTest {

    public void convertConfigWithProxyDomain() throws Exception {
        Document doc = convertXml("/config/test/config.1.3.5_proxy-with-domain.xml",
                new ProxyNTHostConverter());

        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List proxies = root.getChild("proxies", ns).getChildren();
        Element proxyWithoutDomain = (Element) proxies.get(0);
        assertNull(proxyWithoutDomain.getChild("ntHost"), "NTHost element should not exist");

        Element proxyWithDomain = (Element) proxies.get(1);
        Element ntHost = proxyWithDomain.getChild("ntHost", ns);
        assertNotNull(ntHost, "NTHost element should have been added");
        assertEquals(ntHost.getText(), InetAddress.getLocalHost().getHostName(),
                "Should have set the hostname to localhost");
    }
}
