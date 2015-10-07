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

package org.artifactory.mime.version.converter.v1;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * This converter renames the "xml" attribute to "index" and changes the new property value to false for all files
 * except pom and ivy files (this is the default as of version 2 of the mime types file).
 *
 * @author Yossi Shaul
 */
public class XmlIndexedConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List mimetypes = rootElement.getChildren("mimetype", namespace);
        // make sure there are no more 'xml' attributes
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            Attribute xmlAttribute = mimeTypeElement.getAttribute("xml", namespace);
            if (xmlAttribute != null) {
                // rename to index
                xmlAttribute.setName("index");
                // change to false unless maven of ivy
                String type = mimeTypeElement.getAttributeValue("type");
                if (!"application/x-maven-pom+xml".equals(type) && !"application/x-ivy+xml".equals(type)) {
                    xmlAttribute.setValue("false");
                }
            }
        }
    }
}
