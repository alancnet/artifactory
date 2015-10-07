/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.mime.version.converter;

import org.apache.commons.lang.StringUtils;
import org.artifactory.mime.MimeType;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Iterator;
import java.util.List;

/**
 * Base class for mime types converters.
 *
 * @author Yossi Shaul
 */
public abstract class MimeTypeConverterBase implements XmlConverter {

    protected void addIfNotExist(Document doc, MimeType newType) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List mimetypes = rootElement.getChildren("mimetype", namespace);
        if (mimetypes == null) {
            return;
        }

        Element typeElement = getByType(mimetypes, namespace, newType.getType());
        if (typeElement == null) {
            typeElement = new Element("mimetype", namespace);
            typeElement.setAttribute("type", newType.getType());
            typeElement.setAttribute("extensions", buildExtensionsString(newType));
            if (StringUtils.isNotBlank(newType.getCss())) {
                typeElement.setAttribute("css", newType.getCss());
            }
            if (newType.isArchive()) {
                typeElement.setAttribute("archive", newType.isArchive() + "");
            }
            if (newType.isIndex()) {
                typeElement.setAttribute("index", newType.isIndex() + "");
            }
            if (newType.isViewable()) {
                typeElement.setAttribute("viewable", newType.isViewable() + "");
            }
            rootElement.addContent(typeElement);
        }
    }

    private String buildExtensionsString(MimeType newType) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> extIter = newType.getExtensions().iterator();
        while (extIter.hasNext()) {
            sb.append(extIter.next());
            if (extIter.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private Element getByType(List mimetypes, Namespace namespace, String type) {
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            String typeValue = mimeTypeElement.getAttributeValue("type", namespace);
            if (type.equals(typeValue)) {
                return mimeTypeElement;
            }
        }
        return null;
    }
}
