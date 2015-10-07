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

package org.artifactory.update.security.v6;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert the security descriptor with the new group name tag instead of strings
 *
 * @author Tomer Cohen
 */
public class LdapGroupSettingXmlConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace namespace = root.getNamespace();
        Element child = root.getChild("users", namespace);
        List users = child.getChildren("user", namespace);
        if (users != null && !users.isEmpty()) {
            for (Object user : users) {
                Element userElement = (Element) user;
                Element groups = userElement.getChild("groups", namespace);
                if (groups != null) {
                    List groupNames = groups.getChildren("string", namespace);
                    List<String> listOfGroupNames = new ArrayList<>();
                    for (Object groupName : groupNames) {
                        Element group = (Element) groupName;
                        listOfGroupNames.add(group.getText());
                    }
                    groups.removeChildren("string", namespace);
                    for (String name : listOfGroupNames) {
                        Element userGroup = new Element("userGroup", namespace);
                        userGroup.setText(name);
                        groups.addContent(userGroup);
                    }
                }
            }
        }
    }
}
