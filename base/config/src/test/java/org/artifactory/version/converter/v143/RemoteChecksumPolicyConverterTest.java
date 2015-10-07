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

package org.artifactory.version.converter.v143;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests the {@link RemoteChecksumPolicyConverter}.
 *
 * @author Yossi Shaul
 */
@Test
public class RemoteChecksumPolicyConverterTest extends XmlConverterTest {
    public void convertConfigWithNoChecksumPolicyTags() throws Exception {
        // just call the convert, nothing is expected to get changed
        convertXml("/config/install/config.1.4.2.xml", new RemoteChecksumPolicyConverter());
    }

    public void convertConfigWithChecksumPolicyTags() throws Exception {
        // just call the convert, nothing is expected to get changed
        Document doc = convertXml("/config/test/config.1.4.2_with_checksum_policy.xml",
                new RemoteChecksumPolicyConverter());

        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List repositories = root.getChild("remoteRepositories", ns).getChildren();
        Element repoWithPolicy = (Element) repositories.get(0);
        assertNull(repoWithPolicy.getChild("checksumPolicyType", ns), "checksumPolicyType element should not exist");
        Element policyTypeElement = repoWithPolicy.getChild("remoteRepoChecksumPolicyType", ns);
        assertNotNull(policyTypeElement, "Renamed tag not found");
        assertEquals(policyTypeElement.getText(), "generate-if-absent");
    }

}
