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

package org.artifactory.update.security.v4;

import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.UserInfo;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

/**
 * An xml converter that locates any ACE (except anonymous) with admin and deploy permissions, and grants them an
 * annotate permission
 *
 * @author Noam Y. Tenne
 */
public class AnnotatePermissionXmlConverter implements XmlConverter {

    private static final String ACES = "aces";
    private static final String MASK = "mask";
    private static final String PRINCIPAL = "principal";
    private static final String ACE = "ace";
    private static final String GROUP = "group";

    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        Element aclsTag = doc.getRootElement().getChild("acls");
        List<Element> acls = aclsTag.getChildren();
        for (Element acl : acls) {
            Element acesTag = acl.getChild(ACES);
            List<Element> aces = acesTag.getChildren(ACE);
            Element newAces = new Element(ACES);
            Element aceTemplate = new Element(ACE);
            Element groupEl = new Element(GROUP);
            aceTemplate.addContent(new Element(PRINCIPAL)).addContent(groupEl).addContent(new Element(MASK));
            for (Element ace : aces) {
                Element child = ace.getChild("principal");
                Element newAce = (Element) aceTemplate.clone();
                newAce.getChild(PRINCIPAL).setText(ace.getChildText(PRINCIPAL));
                newAce.getChild(GROUP).setText(ace.getChildText(GROUP));

                Element maskEl = ace.getChild(MASK);
                int mask = Integer.parseInt(maskEl.getText());
                if (!child.getText().equals(UserInfo.ANONYMOUS)) {
                    if ((mask & (ArtifactoryPermission.MANAGE.getMask() |
                            ArtifactoryPermission.DEPLOY.getMask())) > 0) {
                        mask |= ArtifactoryPermission.ANNOTATE.getMask();
                    }
                }
                newAce.getChild(MASK).setText("" + mask);
                newAces.addContent(newAce);
            }
            acl.removeChild(ACES);
            acl.addContent(newAces);
        }
    }
}