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

package org.artifactory.update.security.v1;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This converter will add the admin element for admin users.
 *
 * @author Yossi Shaul
 */
public class UserPermissionsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(UserPermissionsConverter.class);

    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        Element root = doc.getRootElement();

        Element usersElement = root.getChild("users");
        if (usersElement == null) {
            log.warn("No users found");
            return;
        }

        List<Element> users = usersElement.getChildren("org.artifactory.security.SimpleUser");
        for (Element user : users) {
            Element authoritiesElement = user.getChild("authorities");
            if (authoritiesElement == null) {
                log.warn("No authorities found for {}", user.getChildText("username"));
                continue;
            }
            List<Element> authorities = authoritiesElement.getChildren("org.acegisecurity.GrantedAuthorityImpl");
            for (Element authority : authorities) {
                if ("ADMIN".equals(authority.getChildText("role"))) {
                    user.addContent(new Element("admin").setText("true"));
                    break;
                }
            }
        }
    }
}
