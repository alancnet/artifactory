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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Renames the failedRetrievalCachePeriodSecs element in remote repo and changes the value to 300 seconds.
 *
 * @author Yossi Shaul
 */
public class AssumedOfflineConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(AssumedOfflineConverter.class);

    @Override
    public void convert(Document doc) {
        log.debug("Converting failedRetrievalCachePeriodSecs");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element remoteRepositoriesElement = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepositoriesElement == null) {
            log.debug("No remote repository exists");
            return;
        }

        List remoteRepositories = remoteRepositoriesElement.getChildren();
        for (Object o : remoteRepositories) {
            Element remoteRepo = (Element) o;
            Element failedRetrievalElement = remoteRepo.getChild("failedRetrievalCachePeriodSecs", namespace);
            if (failedRetrievalElement != null) {
                failedRetrievalElement.setName("assumedOfflinePeriodSecs");
                failedRetrievalElement.setText("300");
            }
        }

        log.debug("Finished converting failedRetrievalCachePeriodSecs");
    }
}
