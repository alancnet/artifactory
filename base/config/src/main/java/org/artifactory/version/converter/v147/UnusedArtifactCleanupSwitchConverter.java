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

package org.artifactory.version.converter.v147;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class UnusedArtifactCleanupSwitchConverter implements XmlConverter {

    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger log = LoggerFactory.getLogger(UnusedArtifactCleanupSwitchConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting the unused artifact cleanup switch conversion");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        log.debug("Converting remote repositories");
        Element remoteRepositoriesElement = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepositoriesElement != null) {
            List<Element> remoteRepositoryElements =
                    remoteRepositoriesElement.getChildren("remoteRepository", namespace);
            if (remoteRepositoryElements != null && !remoteRepositoryElements.isEmpty()) {

                for (Element remoteRepositoryElement : remoteRepositoryElements) {
                    log.debug("Removing unused artifact cleanup switch from '{}'",
                            remoteRepositoryElement.getChild("key", namespace).getText());

                    remoteRepositoryElement.removeChild("unusedArtifactsCleanupEnabled", namespace);
                }
            }
        }

        log.info("Ending the unused artifact cleanup switch conversion");
    }
}
