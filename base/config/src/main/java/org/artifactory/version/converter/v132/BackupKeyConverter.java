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

package org.artifactory.version.converter.v132;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Add a key to the backup elements and remove backups without cronExp. This change is from schema 1.3.2 to 1.3.3.
 *
 * @author Yossi Shaul
 */
public class BackupKeyConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(BackupKeyConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element backupsElement = root.getChild("backups", ns);
        List backups = null;
        if (backupsElement != null) {
            backups = backupsElement.getChildren("backup", ns);
            int generatedKeyIndex = 1;
            Iterator iterator = backups.iterator();
            while (iterator.hasNext()) {
                Element backup = (Element) iterator.next();
                Element cronExp = backup.getChild("cronExp", ns);
                if (cronExp == null) {
                    log.debug("Removing a backup without cron expression");
                    iterator.remove();
                    //backupsElement.removeContent(backup);
                } else {
                    // generate backup unique key and add to the backup element
                    String key = "backup" + generatedKeyIndex++;
                    Element keyElement = new Element("key", ns);
                    keyElement.setText(key);
                    backup.addContent(0, keyElement);
                    log.debug("Generated key '{}' for backup element", key);
                }
            }
        }

        if (backupsElement == null || backups.isEmpty()) {
            log.debug("No backups found");
        }

    }
}
