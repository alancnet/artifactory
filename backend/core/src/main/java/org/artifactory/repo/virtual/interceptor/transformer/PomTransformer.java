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

package org.artifactory.repo.virtual.interceptor.transformer;

import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * POM transformer which removes all not needed tags from both the POM itself and the profile tags. Used to strip the
 * repository deceleration from the POM itself.
 * <p/>
 * IMPORTANT FOR FUTURE EDITORS: WHEN ADDING A SEGMENT REMOVAL MAKE SURE TO USE removeChild(org.jdom2.Element, java.lang.String, org.jdom2.Namespace)
 * SO ALL POM MODIFICATIONS WILL BE TRACKED
 *
 * @author Eli Givoni
 * @author Tomer Cohen
 */
public class PomTransformer {
    private static final Logger log = LoggerFactory.getLogger(PomTransformer.class);

    private final String pomAsString;
    private final PomCleanupPolicy pomCleanupPolicy;

    /**
     * POM modification indicator
     */
    private boolean pomChanged = false;

    public PomTransformer(String pomAsString, PomCleanupPolicy pomCleanupPolicy) {
        if (pomAsString == null) {
            throw new IllegalArgumentException("Null pom content is not allowed");
        }
        this.pomCleanupPolicy = pomCleanupPolicy;
        this.pomAsString = pomAsString;
    }

    public String transform() {
        if (pomCleanupPolicy.equals(PomCleanupPolicy.nothing)) {
            return pomAsString;
        }
        Document pomDocument;
        try {
            //delete repositories and pluginsRepositories
            //Maven model does not preserve layout
            pomDocument = XmlUtils.parse(pomAsString);
        } catch (Exception e) {
            log.warn("Failed to parse pom '{}': ", e.getMessage());
            return pomAsString;
        }
        Element pomRoot = pomDocument.getRootElement();
        Namespace namespace = pomRoot.getNamespace();
        removeChild(pomRoot, "repositories", namespace);
        removeChild(pomRoot, "pluginRepositories", namespace);
        boolean onlyActiveDefault = pomCleanupPolicy.equals(PomCleanupPolicy.discard_active_reference);

        //delete repositories and pluginsRepositories in profiles
        Element profilesElement = pomRoot.getChild("profiles", namespace);
        if (profilesElement != null) {
            List profiles = profilesElement.getChildren();
            for (Object profile : profiles) {
                Element profileElement = (Element) profile;
                if (onlyActiveDefault) {
                    boolean activeByDefault = false;
                    Element activationElement = profileElement.getChild("activation", namespace);
                    if (activationElement != null) {
                        Element activationByDefault = activationElement.getChild("activeByDefault", namespace);
                        if (activationByDefault != null) {
                            activeByDefault = Boolean.parseBoolean(activationByDefault.getText());
                        }
                    }
                    if (activeByDefault) {
                        deleteProfileRepositories(profileElement, namespace);
                    }
                } else {
                    deleteProfileRepositories(profileElement, namespace);
                }
            }
        }

        /**
         * We might have reached here without the pom actually changing, so return the modified xml only if it was
         * Actually modified, otherwise it can result with identical looking POMs that calculate to different checksums
         */
        if (pomChanged) {
            return XmlUtils.outputString(pomDocument);
        } else {
            return pomAsString;
        }
    }

    private void deleteProfileRepositories(Element profile, Namespace namespace) {
        removeChild(profile, "repositories", namespace);
        removeChild(profile, "pluginRepositories", namespace);
    }

    /**
     * Create a central child removal method so that we can also track any changes made to the POM file
     *
     * @param toRemoveFrom  Element to remove a child from
     * @param childToRemove Name of child to remove from element
     * @param namespace     Namespace of current document
     */
    private void removeChild(Element toRemoveFrom, String childToRemove, Namespace namespace) {
        boolean removed = toRemoveFrom.removeChild(childToRemove, namespace);
        if (!pomChanged && removed) {
            pomChanged = true;
        }
    }
}
