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

package org.artifactory.version.converter.v1412;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the indexer intervalByHours property to a cron expression.
 *
 * @author Shay Yaakov
 */
public class IndexerCronExpPropertyConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(IndexerCronExpPropertyConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Converting indexer indexingIntervalHours property to a cron expression based " +
                "configuration descriptor.");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element indexerElement = rootElement.getChild("indexer", namespace);
        if (indexerElement != null) {
            // Remove indexingIntervalHours property
            Element indexingIntervalHours = indexerElement.getChild("indexingIntervalHours", namespace);
            int intervalElementIndex = indexerElement.indexOf(indexingIntervalHours);
            if (indexingIntervalHours != null) {
                indexingIntervalHours.detach();

                // Add cron expression property
                Element cronExpElement = new Element("cronExp", namespace);
                cronExpElement.setText("0 23 5 * * ?");
                indexerElement.addContent(intervalElementIndex, cronExpElement);
            }
        }

        log.info("Finished converting the indexer indexingIntervalHours property.");
    }
}
