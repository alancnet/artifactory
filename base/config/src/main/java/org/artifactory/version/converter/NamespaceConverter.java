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

package org.artifactory.version.converter;

import org.artifactory.version.ArtifactoryConfigVersion;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Changes the namespace and the schema location to the latest.
 *
 * @author Yossi Shaul
 */
public class NamespaceConverter implements XmlConverter {
    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        // change the xsd uri and schema location
        String currentXsdUri = ArtifactoryConfigVersion.getCurrent().getXsdUri();
        String currentXsdLocation = ArtifactoryConfigVersion.getCurrent().getXsdLocation();
        Namespace ns = Namespace.getNamespace(currentXsdUri);
        Element root = doc.getRootElement();
        // Check that schema instance namespace is there before adding schema location...
        Namespace schemaInstanceNS = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        List<Namespace> namespaces = root.getAdditionalNamespaces();
        boolean hasSchemaInstanceNS = false;
        for (Namespace namespace : namespaces) {
            // The equality is only on URI so hardcoded prefix does not impact
            if (namespace.equals(schemaInstanceNS)) {
                hasSchemaInstanceNS = true;
            }
        }
        if (!hasSchemaInstanceNS) {
            root.addNamespaceDeclaration(schemaInstanceNS);
        }
        root.setAttribute("schemaLocation", currentXsdUri + " " + currentXsdLocation, schemaInstanceNS);

        changeNameSpace(root, ns);
    }

    private void changeNameSpace(Element element, Namespace ns) {
        element.setNamespace(ns);
        for (Object childElements : element.getChildren()) {
            changeNameSpace((Element) childElements, ns);
        }
    }
}
