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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Tomer Cohen
 */
public class ProxyDefaultConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(ProxyDefaultConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element proxiesElement = root.getChild("proxies", ns);
        if (proxiesElement == null || proxiesElement.getChildren().isEmpty()) {
            log.debug("No proxies found");
            return;
        }
        List proxies = proxiesElement.getChildren();

        Element repositoriesElement = root.getChild("remoteRepositories", ns);
        List remoteRepos = repositoriesElement.getChildren();
        if (remoteRepos == null || remoteRepos.size() == 0) {
            log.debug("No remote repos found");
            return;
        }

        Element defaultCandidate = null;
        for (Object remoteRepoObj : remoteRepos) {
            Element remoteRepo = (Element) remoteRepoObj;
            Element remoteRepoProxy = remoteRepo.getChild("proxyRef", ns);
            if (remoteRepoProxy == null) {
                //If the remote repository does not have a proxy, we can stop right here.
                return;
            }
            if (defaultCandidate != null && !remoteRepoProxy.getText().equals(defaultCandidate.getText())) {
                return;
            }
            if (defaultCandidate == null) {
                defaultCandidate = remoteRepoProxy;
            }
        }

        for (Object proxyObj : proxies) {
            Element proxy = (Element) proxyObj;
            Element proxyKey = proxy.getChild("key", ns);
            if (proxyKey.getText().equals(defaultCandidate.getText())) {
                if (proxy.getChild("defaultProxy", ns) == null) {   // RTFACT-2450
                    Element element = new Element("defaultProxy", ns);
                    element.setText("true");
                    proxy.addContent(element);
                }
                break;
            }
        }
    }
}
