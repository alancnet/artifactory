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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Sets the ntHost element to be the localhost name if the proxy domain is set.
 *
 * @author Yossi Shaul
 */
public class ProxyNTHostConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(ProxyNTHostConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element proxiesElement = root.getChild("proxies", ns);
        if (proxiesElement != null) {
            List proxies = proxiesElement.getChildren("proxy", ns);
            for (Object proxyObj : proxies) {
                Element proxy = (Element) proxyObj;
                Element domain = proxy.getChild("domain", ns);
                if (domain != null) {
                    Element ntHost = new Element("ntHost", ns);
                    ntHost.setText(getHostName());
                    // insert the ntHost element right before the domain element 
                    proxy.addContent(proxy.indexOf(domain), ntHost);
                }
            }
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Failed to get host name");
            return "unknown";
        }
    }
}
