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

package org.artifactory.update.security.v3;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

/**
 * Converts permission target in an Acls to contain multiple repoKeys.
 *
 * @author Yossi Shaul
 */
public class AclRepoKeysConverter implements XmlConverter {
    @Override
    public void convert(Document doc) {
        Element aclsTag = doc.getRootElement().getChild("acls");
        @SuppressWarnings({"unchecked"})
        List<Element> acls = aclsTag.getChildren();
        for (Element acl : acls) {
            Element permissionTarget = acl.getChild("permissionTarget");
            Element repoKeyElement = permissionTarget.getChild("repoKey");
            permissionTarget.removeContent(repoKeyElement);
            // create the new element - repoKeys and add to the acl
            Element repoKeys = new Element("repoKeys");
            repoKeyElement.setName("string");   // the xstream element name
            repoKeys.addContent(repoKeyElement);
            permissionTarget.addContent(repoKeys);
        }
    }
}
