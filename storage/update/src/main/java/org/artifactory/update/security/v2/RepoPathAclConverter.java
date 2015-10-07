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

import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Renames the tags named after class names to the shorter notation (for example org.acegisecurity.acl.basic.SimpleAclEntry
 * will be renamed to acl). Will convert the permission masks and identifiers.
 *
 * @author freds
 */
public class RepoPathAclConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(RepoPathAclConverter.class);
    private static final String IDENTIFIER = "identifier";
    private static final String ACES = "aces";
    private static final String MASK = "mask";
    private static final String PRINCIPAL = "principal";

    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        Element aclsTag = doc.getRootElement().getChild("acls");
        List<Element> acls = aclsTag.getChildren();
        for (Element acl : acls) {
            if (acl.getName().contains("RepoPathAcl")) {
                acl.setName("acl");
                convertIdentifierToPermissionTarget(acl);
                Element acesTag = acl.getChild(ACES);
                Element aceListTag = acesTag.getChild("list");
                List<Element> aces = aceListTag.getChildren("org.artifactory.security.RepoPathAce");
                Element newAces = new Element(ACES);
                Element aceTemplate = new Element("ace");
                Element groupEl = new Element("group");
                groupEl.setText("false");
                aceTemplate.addContent(new Element(PRINCIPAL)).addContent(groupEl).addContent(new Element(MASK));
                for (Element ace : aces) {
                    Element newAce = (Element) aceTemplate.clone();
                    newAce.getChild(PRINCIPAL).setText(ace.getChildText(PRINCIPAL));
                    Element maskEl = ace.getChild(MASK);
                    int mask = Integer.parseInt(maskEl.getText());
                    if ((mask & (ArtifactoryPermission.MANAGE.getMask() |
                            ArtifactoryPermission.DEPLOY.getMask())) > 0) {
                        mask |= ArtifactoryPermission.DELETE.getMask();
                    }
                    newAce.getChild(MASK).setText("" + mask);
                    newAces.addContent(newAce);
                }
                acl.removeChild(ACES);
                acl.addContent(newAces);
            } else {
                log.warn("Acl tag " + acl + " under acls is not a RepoPAthAcl!");
            }
        }
    }

    private void convertIdentifierToPermissionTarget(Element acl) {
        String identifier;
        try {
            identifier = URLDecoder.decode(acl.getChildText(IDENTIFIER), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to decode identifier", e);
        }
        RepoPath repoPath = InternalRepoPathFactory.fromId(identifier);
        acl.removeChild(IDENTIFIER);
        Element permissionTarget = new Element("permissionTarget");

        Element nameEl = new Element("name");
        if (repoPath.getRepoKey().equalsIgnoreCase(PermissionTargetInfo.ANY_REPO) &&
                repoPath.getPath().equalsIgnoreCase(PermissionTargetInfo.ANY_REPO)) {
            nameEl.setText(PermissionTargetInfo.ANY_PERMISSION_TARGET_NAME);
        } else {
            nameEl.setText(repoPath.getId());
        }
        permissionTarget.addContent(nameEl);

        Element repoKeyEl = new Element("repoKey");
        repoKeyEl.setText(repoPath.getRepoKey());
        permissionTarget.addContent(repoKeyEl);

        Element includesEl = new Element("includes");
        Element includeEl = new Element("string");
        if (repoPath.getPath().equalsIgnoreCase(PermissionTargetInfo.ANY_REPO)) {
            includeEl.setText(PermissionTargetInfo.ANY_PATH);
        } else {
            includeEl.setText(repoPath.getPath() + "/" + PermissionTargetInfo.ANY_PATH);
        }
        includesEl.addContent(includeEl);
        permissionTarget.addContent(includesEl);
        permissionTarget.addContent(new Element("excludes"));

        acl.addContent(permissionTarget);
    }
}
