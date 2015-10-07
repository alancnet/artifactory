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

package org.artifactory.version.converter.v1410;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the gc.interval.secs system property to a cron expression.
 *
 * @author Noam Y. Tenne
 */
public class GcSystemPropertyConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(GcSystemPropertyConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Converting garbage collector system property to a cron expression based configuration descriptor.");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element gcConfigCronExpElement = new Element("cronExp", namespace);
        gcConfigCronExpElement.setText("0 0 /4 * * ?");

        Element gcConfigElement = new Element("gcConfig", namespace);
        gcConfigElement.addContent(gcConfigCronExpElement);

        rootElement.addContent(gcConfigElement);

        log.info("Finished converting the garbage collector system property.");
    }
}
