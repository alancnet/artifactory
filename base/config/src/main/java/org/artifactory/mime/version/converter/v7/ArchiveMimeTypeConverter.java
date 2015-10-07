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

package org.artifactory.mime.version.converter.v7;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Adds the following entries to the mimetypes file if they don't exist:
 * <pre>
 *         <mimetype type="application/x-gzip" extensions="tgz" css="gz"/>
 * </pre>
 * ג
 *
 * @author Chen Keinan
 */
public class ArchiveMimeTypeConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(ArchiveMimeTypeConverter.class);

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List mimetypes = rootElement.getChildren("mimetype", namespace);
        log.info("updating mime-types:  application/x-gzip and application/x-tar , setting archive value to true");
        for (Object mimetype : mimetypes) {
            Element mimeTypeElement = (Element) mimetype;
            String type = mimeTypeElement.getAttributeValue("type", namespace);
            // update gzip and tar archive attribute to true
            if ("application/x-gzip".equals(type) || "application/x-tar".equals(type)) {
                // set archive attribute to true
                mimeTypeElement.getAttribute("archive").setValue("true");
            }
        }
    }
}
