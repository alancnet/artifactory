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

package org.artifactory.version.converter.v149;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Converts the remote repo replication element names that have changed between v148 and v149 (introduction of local
 * repo replication)
 *
 * @author Noam Y. Tenne
 */
public class ReplicationElementNameConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(ReplicationElementNameConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting to convert old remote repository replication configurations.");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element replications = rootElement.getChild("replications", namespace);
        if (replications != null) {
            replications.setName("remoteReplications");

            List<Element> replicationList = replications.getChildren("replication", namespace);
            if (replicationList != null) {
                for (Element replication : replicationList) {
                    replication.setName("remoteReplication");
                }
            }
        }
        log.info("Finished converting old remote repository replication configurations.");
    }
}
