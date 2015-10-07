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

package org.artifactory.version;

import org.artifactory.api.version.ArtifactoryVersioning;
import org.artifactory.api.version.VersionHolder;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

/**
 * A tool to parse versioning info XML into an ArtifactoryVersioning
 *
 * @author Noam Tenne
 */
public abstract class VersionParser {
    private VersionParser() {
        // utility class
    }

    /**
     * Parses the received input string info an ArtifactoryVersioning object
     *
     * @param input String containing the XML of the versioning info file
     * @return ArtifactoryVersioning - Object representation of the versioning info xml
     */
    public static ArtifactoryVersioning parse(String input) {
        Document doc = XmlUtils.parse(input);
        Element root = doc.getRootElement();
        Element version = root.getChild("versioning");
        if (version == null) {
            throw new RuntimeException("No version is defined");
        }

        VersionHolder latest = null;
        VersionHolder release = null;
        List children = version.getChildren();
        for (Object child : children) {
            Element holder = (Element) child;
            String versionNumber = holder.getChildText("version");
            String revisionNumber = holder.getChildText("revision");
            String wikiUrl = holder.getChildText("wikiUrl");
            String downloadUrl = holder.getChildText("downloadUrl");
            if ("latest".equals(holder.getName())) {
                latest = new VersionHolder(versionNumber, revisionNumber, wikiUrl, downloadUrl);
            } else if ("release".equals(holder.getName())) {
                release = new VersionHolder(versionNumber, revisionNumber, wikiUrl, downloadUrl);
            }
        }

        if ((latest == null) || (release == null)) {
            throw new IllegalArgumentException("Latest and stable version and revisions are not defined properly");
        }

        return new ArtifactoryVersioning(latest, release);
    }
}
