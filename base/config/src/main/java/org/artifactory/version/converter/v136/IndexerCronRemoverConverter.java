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

package org.artifactory.version.converter.v136;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replace the cronExp for the indexer with an interval. Applies for upgrading from schema 1.3.5 to 1.3.6.
 *
 * @author yoavl
 */
public class IndexerCronRemoverConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(IndexerCronRemoverConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        /*
        <indexer>
            <cronExp>0 /1 * * * ?</cronExp>
        </indexer>
        to:
        <indexer>
            <indexingIntervalHours>24</indexingIntervalHours>
        </indexer>
         */
        Element indexerElement = root.getChild("indexer", ns);
        if (indexerElement != null) {
            log.debug("Removing indexer cron expression.");
            indexerElement.removeContent();
            log.debug("Adding default indexer interval.");
            Element intervalElement = new Element("indexingIntervalHours", ns);
            intervalElement.setText("24");
            indexerElement.addContent(0, intervalElement);
        }
    }
}