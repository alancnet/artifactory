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

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests {@link org.artifactory.version.converter.v149.ReplicationElementNameConverter}
 *
 * @author Noam Y. Tenne
 */
@Test
public class ReplicationElementNameConverterTest extends XmlConverterTest {

    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.1.4.8_old_replication.xml",
                new ReplicationElementNameConverter());

        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        assertNull(rootElement.getChild("replications", namespace),
                "Found the old replication configuration name the was supposed to be converted");

        Element replications = rootElement.getChild("remoteReplications", namespace);
        assertNotNull(replications, "Expected to find the converted remote replications element");

        assertTrue(replications.getChildren("replication", namespace).isEmpty(),
                "Found the old replication configuration name the was supposed to be converted");

        List<Element> remoteReplicationList = replications.getChildren("remoteReplication", namespace);
        assertNotNull(remoteReplicationList, "Expected to find replication configurations");
        assertFalse(remoteReplicationList.isEmpty(), "Expected to find replication configurations");
    }
}
