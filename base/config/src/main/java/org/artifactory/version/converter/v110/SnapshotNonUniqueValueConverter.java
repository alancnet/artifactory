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

package org.artifactory.version.converter.v110;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <pre>
 * Converts:
 * &lt;snapshotVersionBehavior&gt;nonunique&lt;/snapshotVersionBehavior&gt;
 * Into:
 * &lt;snapshotVersionBehavior&gt;non-unique&lt;/snapshotVersionBehavior&gt;
 * </pre>
 * <p/>
 * The element snapshotVersionBehavior might appear under a localRepository element. Was valid until version 1.1.0 of
 * the schema.
 *
 * @author Yossi Shaul
 */
public class SnapshotNonUniqueValueConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(SnapshotNonUniqueValueConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        List localRepos = root.getChild("localRepositories", ns).getChildren();
        for (Object localRepo1 : localRepos) {
            Element localRepo = (Element) localRepo1;
            Element snapshotBehavior = localRepo.getChild("snapshotVersionBehavior", ns);
            if (snapshotBehavior != null && "nonunique".equals(snapshotBehavior.getText())) {
                log.debug("Changing value 'nonunique' to 'non-unique' for repo {}",
                        localRepo.getChildText("key", ns));
                snapshotBehavior.setText("non-unique");
            }
        }
    }
}