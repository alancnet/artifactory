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

package org.artifactory.mime.version.converter.v2;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * This converter adds the .asc extension to the "text/plain" mimetype
 *
 * @author Shay Yaakov
 */
public class AscMimeTypeConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List mimetypes = rootElement.getChildren("mimetype", namespace);
        // find "text/plain" mimetype
        if (mimetypes != null) {
            for (Object mimetype : mimetypes) {
                Element mimeTypeElement = (Element) mimetype;
                String type = mimeTypeElement.getAttributeValue("type");
                if ("text/plain".equals(type)) {
                    String extensions = mimeTypeElement.getAttributeValue("extensions", namespace);
                    if (StringUtils.isBlank(extensions)) {
                        extensions = "";
                    } else {
                        if (!extensions.endsWith(",") && !extensions.endsWith(", ")) {
                            extensions += ", ";
                        }
                    }
                    mimeTypeElement.setAttribute("extensions", extensions + "asc");
                    break;
                }
            }
        }
    }
}
