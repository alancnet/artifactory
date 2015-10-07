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

import org.apache.commons.lang.math.RandomUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the repo.cleanup.intervalHours system property to a cron expression,
 * Using a random hour between 04:00-05:59 so each AOL client will run on a different time
 *
 * @author Shay Yaakov
 */
public class CleanupConfigConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(CleanupConfigConverter.class);

    @Override
    public void convert(Document doc) {
        log.debug("Converting artifacts cleanup system property to a cron expression based configuration descriptor.");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        // Create cron expression element with random times from 04:00AM to 05:59AM
        Element cronExpElement = new Element("cronExp", namespace);
        int minutes = RandomUtils.nextInt(60); // 0-59
        int hours = RandomUtils.nextInt(2) + 4; // 4-5
        cronExpElement.setText("0 " + minutes + " " + hours + " * * ?");

        Element cleanupElement = new Element("cleanupConfig", namespace);
        cleanupElement.addContent(cronExpElement);

        rootElement.addContent(cleanupElement);

        log.debug("Finished converting the artifacts cleanup system property.");
    }
}
