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

package org.artifactory.logging.version.v3;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Adds org.jfrog package as info level to the logback.xml file.
 *
 * @author Shay Yaakov
 */
public class LogbackJFrogInfoConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List<Element> loggers = root.getChildren("logger", ns);
        for (Element logger : loggers) {
            if (StringUtils.equals(logger.getAttributeValue("name", ns), "org.jfrog")) {
                return;
            }
        }

        // Did not find org.jfrog, let's add it
        Element logger = new Element("logger", ns);
        logger.setAttribute("name", "org.jfrog");
        Element level = new Element("level", ns);
        level.setAttribute("value", "info");
        logger.addContent(level);
        root.addContent(logger);
    }
}