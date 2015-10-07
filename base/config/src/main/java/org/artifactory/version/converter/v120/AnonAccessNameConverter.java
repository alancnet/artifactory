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

package org.artifactory.version.converter.v120;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renames the element "anonDownloadsAllowed" to "anonAccessEnabled". This element was directly under the root element.
 * Was valid until version 1.2.0 of the schema.
 *
 * @author Yossi Shaul
 */
public class AnonAccessNameConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(AnonAccessNameConverter.class);

    private static final String OLD_ANNON = "anonDownloadsAllowed";
    private static final String NEW_ANNON = "anonAccessEnabled";

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Element oldAnnon = root.getChild(OLD_ANNON, root.getNamespace());
        if (oldAnnon != null) {
            oldAnnon.setName(NEW_ANNON);
            log.debug("Element {} found and converted", OLD_ANNON);
        } else {
            log.debug("Element {} not found", OLD_ANNON);
        }
    }
}
