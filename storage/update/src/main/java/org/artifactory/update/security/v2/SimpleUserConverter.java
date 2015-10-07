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

package org.artifactory.update.security.v2;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Renames the org.artifactory.security.SimpleUser tag to user and removes the authorities.
 *
 * @author freds
 */
public class SimpleUserConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(SimpleUserConverter.class);

    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        Element usersTag = doc.getRootElement().getChild("users");
        List<Element> users = usersTag.getChildren();
        for (Element user : users) {
            if (user.getName().contains("SimpleUser")) {
                user.setName("user");
                user.removeChild("authorities");
            } else {
                log.warn("A tag " + user + " under users is not SimpleUSer!!");
            }
        }
    }
}
