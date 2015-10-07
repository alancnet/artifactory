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

package org.artifactory.mime.version.converter.v3;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Adds 'index="true"' attribute to all the mime types that marked as archive.
 *
 * @author Yossi Shaul
 */
public class ArchivesIndexConverter implements XmlConverter {
    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List mimetypes = rootElement.getChildren("mimetype", namespace);
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            String archiveValue = mimeTypeElement.getAttributeValue("archive", namespace);
            if ("true".equals(archiveValue)) {
                // add index attribute
                mimeTypeElement.setAttribute("index", "true", namespace);
            }
        }
    }
}
