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

import org.artifactory.api.security.SecurityService;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * If the default Anything permission target exists and the anonymous user read permission is set, add the new default
 * "any remote" premission and grant read+deploy permissions on it to anonymous. This is how the additionan element
 * looks like:
 * <pre>
 *   <acl>
 *     <permissionTarget>
 *       <name>Any Remote</name>
 *       <repoKey>ANY REMOTE</repoKeys>
 *       <includes>
 *         <string>**</string>
 *       </includes>
 *       <excludes/>
 *     </permissionTarget>
 *     <aces>
 *       <ace>
 *         <principal>anonymous</principal>
 *         <group>false</group>
 *         <mask>3</mask>
 *       </ace>
 *     </aces>
 *     <updatedBy>system</updatedBy>
 *   </acl>
 * </pre>
 *
 * @author Yossi Shaul
 */
public class AnyRemoteConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(AnyRemoteConverter.class);

    @Override
    public void convert(Document doc) {
        Element aclsTag = doc.getRootElement().getChild("acls");

        if (shouldAddDefaultAnyRemote(aclsTag)) {
            Element acl = new Element("acl");
            Element permissionTarget = new Element("permissionTarget");
            permissionTarget.addContent(newElement("name", "Any Remote"));
            permissionTarget.addContent(newElement("repoKey", "ANY REMOTE"));
            Element includes = new Element("includes");
            includes.addContent(newElement("string", "**"));
            permissionTarget.addContent(includes);
            permissionTarget.addContent(new Element("excludes"));
            acl.addContent(permissionTarget);

            Element aces = new Element("aces");
            Element ace = new Element("ace");
            ace.addContent(newElement("principal", "anonymous"));
            ace.addContent(newElement("group", "false"));
            ace.addContent(newElement("mask", "3"));    // read+write
            aces.addContent(ace);
            acl.addContent(aces);
            acl.addContent(newElement("updatedBy", SecurityService.USER_SYSTEM));

            aclsTag.addContent(acl);
        } else {
            log.debug("No need to add default any remote permissions");
        }
    }

    @SuppressWarnings({"unchecked"})
    private boolean shouldAddDefaultAnyRemote(Element aclsTag) {
        List<Element> acls = aclsTag.getChildren();
        for (Element acl : acls) {
            Element permissionTarget = acl.getChild("permissionTarget");
            String repoKey = permissionTarget.getChildText("repoKey");
            if ("ANY".equals(repoKey)) {
                Element acesTag = acl.getChild("aces");
                if (acesTag != null) {
                    List<Element> aces = acesTag.getChildren();
                    for (Element ace : aces) {
                        if ("anonymous".equals(ace.getChildText("principal"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Element newElement(String name, String value) {
        Element element = new Element(name);
        element.setText(value);
        return element;
    }

}