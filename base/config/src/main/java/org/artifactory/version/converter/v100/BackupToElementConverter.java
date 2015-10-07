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

package org.artifactory.version.converter.v100;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * Convers
 *    &lt;backupDir&gt;XX&lt;/backupDir&gt;
 *    &lt;backupCronExp&gt;YY&lt;/backupCronExp&gt;
 * to:
 *    &lt;backup&gt;
 *        &lt;dir&gt;XX&lt;/dir&gt;
 *        &lt;cronExp&gt;YY&lt;/cronExp&gt;
 *    &lt;/backup&gt;
 * </pre>
 * <p/>
 * Those backup elements were directly under the root in version 1.0.0 of the schema. The new backup element should be
 * placed before the localRepositories element.
 *
 * @author Yossi Shaul
 */
public class BackupToElementConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(BackupToElementConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element backupDir = root.getChild("backupDir", ns);
        if (backupDir != null) {
            root.removeContent(backupDir);
            backupDir.setName("dir");
            log.debug("Renamed 'backupDir' to 'dir'");
        }

        Element backupCron = root.getChild("backupCronExp", ns);
        if (backupCron != null) {
            root.removeContent(backupCron);
            backupCron.setName("cronExp");
            log.debug("Renamed 'backupCronExp' to 'cronExp'");
        }

        if (backupDir != null && backupCron != null) {
            // create the new <backup> element and place before the localRepositories
            Element backup = new Element("backup", ns);
            backup.addContent(backupDir);
            backup.addContent(backupCron);
            int localReposLocation = root.indexOf(root.getChild("localRepositories", ns));
            root.addContent(localReposLocation, backup);
        } else {
            log.debug("No backup elements found");
        }
    }
}
