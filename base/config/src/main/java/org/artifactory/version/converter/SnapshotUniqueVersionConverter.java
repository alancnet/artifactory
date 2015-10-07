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

package org.artifactory.version.converter;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <pre>
 * Convert
 * &lt;useSnapshotUniqueVersions&gt;true&lt;/useSnapshotUniqueVersions&gt;
 * to
 * &lt;snapshotVersionBehavior&gt;deployer&lt;/snapshotVersionBehavior&gt;
 * and
 * &lt;useSnapshotUniqueVersions&gt;false&lt;/useSnapshotUniqueVersions&gt;
 * to
 * &lt;snapshotVersionBehavior&gt;non-unique&lt;/snapshotVersionBehavior&gt;
 * </pre>
 * Rename the element "useSnapshotUniqueVersions" to "snapshotVersionBehavior" and change the values "true" to
 * "deployer" and "false" to "non-unique".
 * <p/>
 * The element might appear under the localRepo element. Was valid only in version 1.0.0 of the schema.
 *
 * @author Yossi Shaul
 */
public class SnapshotUniqueVersionConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(SnapshotUniqueVersionConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List localRepos = root.getChild("localRepositories", ns).getChildren();
        for (Object localRepo1 : localRepos) {
            Element localRepo = (Element) localRepo1;
            Element snapshotBehavior = localRepo.getChild("useSnapshotUniqueVersions", ns);
            if (snapshotBehavior != null) {
                // rename the element
                snapshotBehavior.setName("snapshotVersionBehavior");
                String repoKey = localRepo.getChildText("key", ns);
                log.debug("Renamed element 'useSnapshotUniqueVersions' to " +
                        "'snapshotVersionBehavior' for repo {}", repoKey);

                // change the element value
                if (snapshotBehavior.getText().equals("true")) {
                    log.debug("Changed value 'true' to 'deployer' for repo {}", repoKey);
                    snapshotBehavior.setText("deployer");
                } else if (snapshotBehavior.getText().equals("false")) {
                    log.debug("Changed value 'false' to 'non-unique' for repo {}", repoKey);
                    snapshotBehavior.setText("non-unique");
                }
            }
        }
    }
}
