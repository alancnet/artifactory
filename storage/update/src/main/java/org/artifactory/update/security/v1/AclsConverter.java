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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renames the tags named after class names to the shorter notation (for example org.acegisecurity.acl.basic.SimpleAclEntry
 * will be renamed to acl). Will convert the permission masks and identifiers.
 *
 * @author freds
 */
public class AclsConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(AclsConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        root.removeChild("repoPaths");

        Element aclsElement = root.getChild("acls");
        @SuppressWarnings({"unchecked"})
        List<Element> acls = aclsElement.getChildren();
        if (acls == null) {
            log.warn("No acls detected");
            return;
        }

        // remove all acls - will create new ones
        root.removeChild("acls");
        Element newAclsElement = new Element("acls");
        root.addContent(newAclsElement);

        Map<Integer, String> objectIdentitiesMap = buildObjectIdentitiesMap(acls);
        HashMap<String, Element> newAclsByIdentity = new HashMap<>();

        for (Element oldAcl : acls) {
            if ("org.acegisecurity.acl.basic.SimpleAclEntry".equals(oldAcl.getName())) {
                Element objectIdentity = oldAcl.getChild("aclObjectIdentity");
                if (hasIdentity(objectIdentity)) {
                    // add new acl
                    Element newAcl = new Element("org.artifactory.security.RepoPathAcl");
                    String newIdentity = getNewIdentity(objectIdentity);
                    newAcl.addContent(new Element("identifier").setText(newIdentity));
                    Element aces = new Element("aces");
                    newAcl.addContent(aces);
                    aces.addContent(new Element("list"));
                    addAceToAcl(oldAcl, newAcl);
                    newAclsByIdentity.put(newIdentity, newAcl);
                    newAclsElement.addContent(newAcl);
                } else {
                    // just an ace - add to existing acl
                    // get the identifier index from the reference attribute
                    // sample value: ../../org.acegisecurity.acl.basic.SimpleAclEntry[2]/aclObjectIdentity
                    String reference = objectIdentity.getAttributeValue("reference");
                    Pattern pattern = Pattern.compile(".*org.acegisecurity.acl.basic.SimpleAclEntry\\[(.*)\\].*");
                    Matcher matcher = pattern.matcher(reference);
                    if (matcher.matches()) {
                        String indexStr = matcher.group(1);
                        int identityIndex = Integer.parseInt(indexStr);
                        String identity = objectIdentitiesMap.get(identityIndex);
                        Element newAcl = newAclsByIdentity.get(identity);
                        addAceToAcl(oldAcl, newAcl);
                    } else {
                        log.warn("Couldn't match identity reference {}", reference);
                    }
                }
            } else {
                log.warn("Acl tag " + oldAcl + " under acls is not a SimpleAclEntry!");
            }
        }
    }

    private void addAceToAcl(Element oldAcl, Element newAcl) {
        Element acesListElement = newAcl.getChild("aces").getChild("list");
        String user = oldAcl.getChildText("recipient");
        String mask = oldAcl.getChildText("mask");
        if ("___INHERITENCE_MARKER_ONLY___".equals(user)) {
            user = "anonymous";
            mask = "1";
        }
        Element ace = new Element("org.artifactory.security.RepoPathAce");
        ace.addContent(new Element("principal").setText(user));
        ace.addContent(new Element("mask").setText(mask));
        acesListElement.addContent(ace);
    }

    private boolean hasIdentity(Element objectIdentity) {
        return objectIdentity.getChild("repoKey") != null;
    }

    private Map<Integer, String> buildObjectIdentitiesMap(List<Element> acls) {
        HashMap<Integer, String> identities = new HashMap<>();
        for (int i = 0; i < acls.size(); i++) {
            Element acl = acls.get(i);
            Element objectIdentity = acl.getChild("aclObjectIdentity");
            if (hasIdentity(objectIdentity)) {
                String identity = getNewIdentity(objectIdentity);
                identities.put(i + 1, identity);
            }
        }
        return identities;
    }

    private String getNewIdentity(Element objectIdentity) {
        String repoKey = objectIdentity.getChildText("repoKey");
        String path = objectIdentity.getChildText("path");
        String identity = repoKey + "%3a" + path;
        return identity;
    }
}