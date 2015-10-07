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

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Renamed the checksum policy tag of remote repositories:<p/>
 * This:<p/>
 * &lt;checksumPolicyType&gt;generate-if-absent&lt;/checksumPolicyType&gt;
 * <p/>Will become:<p/>
 * &lt;remoteRepoChecksumPolicyType&gt;generate-if-absent&lt;/remoteRepoChecksumPolicyType&gt;
 *
 * @author Yossi Shaul
 */
public class RemoteChecksumPolicyConverter implements XmlConverter {
    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element remoteReposElement = root.getChild("remoteRepositories", ns);
        if (remoteReposElement != null) {
            List remoteRepos = remoteReposElement.getChildren("remoteRepository", ns);
            for (Object remoteRepoObj : remoteRepos) {
                Element remoteRepo = (Element) remoteRepoObj;
                Element checksumPolicy = remoteRepo.getChild("checksumPolicyType", ns);
                if (checksumPolicy != null) {
                    checksumPolicy.setName("remoteRepoChecksumPolicyType");
                }
            }
        }
    }
}
