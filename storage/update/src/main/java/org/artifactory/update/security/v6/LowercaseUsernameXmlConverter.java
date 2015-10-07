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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Cohen
 */
public class LowercaseUsernameXmlConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        // 1. aggregate all usenames in a map (username->user element)
        // 2. for each user check if another lowercase username exists in the map
        // 2.1 if duplicates doesn't exist, add to the users SET/MAP
        // 2.2 if duplicates exist, take the admin if there is one (first if not)
        // 3. attach the users elements to the "users" element (cut it first)

        // in the acls:
        // 1. for each ace in acl:
        // 1.1 if no duplicate -> add to the list of aces
        // 1.2 if there are duplicates -> merge the mask eg a | b (101 | 010 = 111)


        // for groups:
        // 1. Remove duplicates under groups element (change all to lowercase)
        // 2. Remove duplicates under users groups element (change all to lowercase)

        // merge aces (same for groups and for users)


        Element root = doc.getRootElement();
        Namespace namespace = root.getNamespace();

        mergeGroups(root, namespace);
        mergeUsers(root, namespace);
        mergeAcls(root, namespace);
    }

    private void mergeUsers(Element root, Namespace namespace) {
        Element child = root.getChild("users", namespace);
        List users = child.getChildren("user", namespace);
        Map<String, Element> usernameToGroups = Maps.newHashMap();
        if (users != null && !users.isEmpty()) {
            Iterator iterator = users.iterator();
            while (iterator.hasNext()) {
                Element userElement = (Element) iterator.next();
                Element userNameElement = userElement.getChild("username", namespace);
                String userName = userNameElement.getText();
                String lowerCaseUsername = userName.toLowerCase();
                userNameElement.setText(lowerCaseUsername);
                if (!usernameToGroups.containsKey(lowerCaseUsername)) {
                    usernameToGroups.put(lowerCaseUsername, userElement);
                    addGroupsToUser(userElement, userElement, namespace);
                    copyEmails(namespace, usernameToGroups, userElement, lowerCaseUsername);
                } else {
                    String isAdmin = userElement.getChild("admin").getText();
                    Element existingUserElement = usernameToGroups.get(lowerCaseUsername);
                    addGroupsToUser(existingUserElement, userElement, namespace);
                    if (Boolean.parseBoolean(isAdmin)) {
                        usernameToGroups.put(lowerCaseUsername, userElement);
                    }
                    copyEmails(namespace, usernameToGroups, existingUserElement, lowerCaseUsername);
                    addGroupsToUser(userElement, existingUserElement, namespace);
                    iterator.remove();
                }
            }
        }
        root.removeChildren("users", namespace);
        Element usersElement = new Element("users", namespace);
        root.addContent(usersElement);
        for (Map.Entry<String, Element> elementEntry : usernameToGroups.entrySet()) {
            Element newUser = elementEntry.getValue();
            Element userElement = new Element("user", namespace);
            userElement.setContent(newUser.cloneContent());
            usersElement.addContent(userElement);
        }
    }

    private void copyEmails(Namespace namespace, Map<String, Element> usernameToGroups, Element userElement,
            String lowerCaseUsername) {
        Element userElementFromMap = usernameToGroups.get(lowerCaseUsername);
        Element emailFromXml = userElementFromMap.getChild("email");
        if (emailFromXml == null) {
            Element emailElement = userElement.getChild("email");
            if (emailElement != null) {
                String email = emailElement.getText();
                if (StringUtils.isNotBlank(email)) {
                    Element newEmailElement = new Element("email", namespace);
                    newEmailElement.setText(email);
                    userElementFromMap.addContent(newEmailElement);
                }
            }
        }
    }

    private Map<String, Element> mergeGroups(Element root, Namespace namespace) {
        Map<String, Element> foundGroupNames = Maps.newHashMap();
        Element groupsChildren = root.getChild("groups", namespace);
        if (groupsChildren != null) {
            List groups = groupsChildren.getChildren("group", namespace);
            if (groups != null && !groups.isEmpty()) {
                Iterator groupsIterator = groups.iterator();
                while (groupsIterator.hasNext()) {
                    Element groupElement = (Element) groupsIterator.next();
                    Element groupNameElement = groupElement.getChild("groupName");
                    String groupName = groupNameElement.getText();
                    String lowerCaseGroupName = groupName.toLowerCase();
                    if (!foundGroupNames.containsKey(lowerCaseGroupName)) {
                        groupNameElement.setText(lowerCaseGroupName);
                        foundGroupNames.put(lowerCaseGroupName, groupElement);
                    } else {
                        Element autoJoinElement = groupElement.getChild("newUserDefault");
                        if (autoJoinElement != null) {
                            boolean isNewUserDefault = Boolean.parseBoolean(autoJoinElement.getText());
                            if (isNewUserDefault) {
                                Element existingGroupElement = foundGroupNames.get(lowerCaseGroupName);
                                Element existingNewUserDefault = existingGroupElement.getChild("newUserDefault");
                                if (existingNewUserDefault != null) {
                                    existingNewUserDefault.setText("true");
                                } else {
                                    Element newUserDefault = new Element("newUserDefault");
                                    newUserDefault.setText("true");
                                    existingGroupElement.addContent(newUserDefault);
                                }
                            }
                        }
                        groupsIterator.remove();
                    }
                }
            }
        }
        return foundGroupNames;
    }

    private void addGroupsToUser(Element userElement, Element userToGetGroupsFrom, Namespace namespace) {
        Set<String> existingGroupNames = getUserGroups(userElement);
        Set<String> newGroupNames = getUserGroups(userToGetGroupsFrom);
        existingGroupNames.addAll(newGroupNames);
        Element groupsElement = userElement.getChild("groups");
        if (groupsElement != null) {
            groupsElement.removeChildren("userGroup");
            for (String groupName : existingGroupNames) {
                Element userGroup = new Element("userGroup", namespace);
                userGroup.setText(groupName.toLowerCase());
                groupsElement.addContent(userGroup);
            }
        }
    }

    private Set<String> getUserGroups(Element userElement) {
        Set<String> groups = Sets.newHashSet();
        Element groupsElement = userElement.getChild("groups");
        if (groupsElement != null) {
            List userGroups = groupsElement.getChildren("userGroup");
            if (userGroups != null && !userGroups.isEmpty()) {
                for (Object group : userGroups) {
                    Element groupElement = (Element) group;
                    groups.add(groupElement.getText().toLowerCase());
                }
            }
        }
        return groups;
    }

    private void mergeAcls(Element root, Namespace namespace) {
        List acls = root.getChildren("acls", namespace);
        if (acls != null && !acls.isEmpty()) {
            for (Object acl : acls) {
                Element aclElement = (Element) acl;
                List aces = aclElement.getChildren("acl", namespace);
                if (aces != null && !aces.isEmpty()) {
                    for (Object ace : aces) {
                        Element aceElement = (Element) ace;
                        List childAces = aceElement.getChildren("aces", namespace);
                        if (childAces != null && !childAces.isEmpty()) {
                            for (Object childAce : childAces) {
                                Map<String, Element> aclMap = Maps.newHashMap();

                                Element childAceElement = (Element) childAce;
                                List aceName = childAceElement.getChildren("ace");
                                if (aceName != null && !aceName.isEmpty()) {
                                    Iterator iterator = aceName.iterator();
                                    while (iterator.hasNext()) {
                                        Element child = (Element) iterator.next();
                                        Element principalElement = child.getChild("principal");
                                        boolean isGroup = Boolean.parseBoolean(child.getChild("group").getText());
                                        String principal = principalElement.getText();
                                        String principalLowerCase = principal.toLowerCase();
                                        String finalGroupName = principalLowerCase + " " + isGroup;
                                        principalElement.setText(principalLowerCase);
                                        if (!aclMap.containsKey(finalGroupName)) {
                                            aclMap.put(finalGroupName, child);
                                        } else {
                                            Element element = aclMap.get(finalGroupName);
                                            int newMask = Integer.parseInt(child.getChild("mask").getText());
                                            Element originalElementMask = element.getChild("mask");
                                            int originalMask = Integer.parseInt(originalElementMask.getText());
                                            newMask |= originalMask;
                                            originalElementMask.setText(String.valueOf(newMask));
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}