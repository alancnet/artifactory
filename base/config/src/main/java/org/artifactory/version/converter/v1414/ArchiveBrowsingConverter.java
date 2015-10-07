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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Removes the archiveBrowsingEnabled element, we now use it for each repository separately.
 *
 * @author Shay Yaakov
 */
public class ArchiveBrowsingConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element securityElement = rootElement.getChild("security", namespace);
        if (securityElement != null) {
            Element archiveBrowsingEnabledElement = securityElement.getChild("archiveBrowsingEnabled", namespace);
            if (archiveBrowsingEnabledElement != null) {
                archiveBrowsingEnabledElement.detach();
            }
        }
    }
}
