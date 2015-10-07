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

package org.artifactory.version.converter.v130;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * <pre>
 * Converts:
 *   &lt;backup&gt;...&lt;/backup&gt;
 * into:
 *   &lt;backups&gt;
 *     &lt;backup&gt;...&lt;/backup&gt;
 *   &lt;/backups&gt;
 * </pre>
 * The backup element was directly under the root element. Was valid until version 1.3.0 of the schema.
 *
 * @author Yossi Shaul
 */
public class BackupListConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element backup = root.getChild("backup", ns);
        if (backup != null) {
            int location = root.indexOf(backup);
            root.removeContent(location);
            Element backups = new Element("backups", ns);
            backups.addContent(backup);
            root.addContent(location, backups);
        }
    }
}
