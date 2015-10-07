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

package org.artifactory.version.converter.v136;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Relocates the key element of the remote repositories. Applies for upgrading from schema 1.3.5 to 1.3.6.
 *
 * @author yossis
 */
public class RepositoryTypeConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(RepositoryTypeConverter.class);

    /**
     * Convert a &lt;type&gt; on a remote repository to a &lt;type&gt; on any repo - move the type after the description
     * if it exists.
     *
     * @param doc
     */
    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element remoteRepositories = root.getChild("remoteRepositories", ns);
        if (remoteRepositories != null) {
            List repos = remoteRepositories.getChildren("remoteRepository", ns);
            for (Object repo : repos) {
                Element repoElem = (Element) repo;
                Element type = repoElem.getChild("type", ns);
                if (type != null) {
                    log.debug("Relocating type...");
                    repoElem.removeChild("type", ns);
                    //Try to place it first after the decription if exists, else after the key
                    Element sibling = repoElem.getChild("description", ns);
                    if (sibling == null) {
                        sibling = repoElem.getChild("key", ns);
                    }
                    if (sibling != null) {
                        repoElem.addContent(repoElem.indexOf(sibling) + 1, type);
                        log.debug("Type relocated.");
                    } else {
                        log.warn("Type could be relocated - cannot determine proper location.");
                    }
                }
            }
        }
    }
}