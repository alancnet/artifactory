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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the blackduck settings section to schema 1.5.3 by adding the default proxy, if such exists.
 *
 * @author Yoav Luft
 */
public class BlackDuckProxyConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(BlackDuckProxyConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Converting BlackDuck intergration proxy settings");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element defaultProxy = findDefaultProxy(rootElement, namespace);
        Element externalProviders = rootElement.getChild("externalProviders", namespace);
        if (externalProviders == null) {
            return;
        }
        Element blackDuckConf = externalProviders.getChild("blackduck", namespace);
        if (defaultProxy != null && blackDuckConf != null) {
            if (blackDuckConf.getChild("proxyRef") != null) {
                return;
            }
            Element proxyTag = new Element("proxyRef", namespace);
            proxyTag.setText(defaultProxy.getChildText("key", namespace));
            blackDuckConf.addContent(proxyTag);
        }
    }

    private Element findDefaultProxy(Element rootElement, Namespace namespace) {
        Element proxies = rootElement.getChild("proxies", namespace);
        if (proxies != null) {
            for (Object proxy : proxies.getChildren("proxy", namespace)) {
                if (proxy instanceof Element) {
                    Element proxyElement = (Element) proxy;
                    if ("true".equalsIgnoreCase(proxyElement.getChildText("defaultProxy", namespace))) {
                        return proxyElement;
                    }
                }
            }
        }
        return null;
    }
}
