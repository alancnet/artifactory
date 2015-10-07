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

package org.artifactory.version.converter.v142;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * @author Eli Givoni
 */
public class RepoIncludeExcludePatternsConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element localReposElement = root.getChild("localRepositories", ns);
        List localRepos = localReposElement == null ? null : localReposElement.getChildren("localRepository", ns);

        Element remotelReposElement = root.getChild("remoteRepositories", ns);
        List remoteRepos = remotelReposElement == null ? null : remotelReposElement.getChildren("remoteRepository", ns);

        movePatternsElements(localRepos, ns);
        movePatternsElements(remoteRepos, ns);
    }

    private void movePatternsElements(List repos, Namespace ns) {
        if (repos != null) {
            for (Object repo : repos) {
                Element repoElement = (Element) repo;

                Element includesPattern = repoElement.getChild("includesPattern", ns);
                Element excludePattern = repoElement.getChild("excludesPattern", ns);

                setPatternsElements(repoElement, includesPattern, ns);
                setPatternsElements(repoElement, excludePattern, ns);
            }
        }
    }

    private void setPatternsElements(Element repoElement, Element patternElement, Namespace ns) {
        if (patternElement == null) {
            return;
        }
        repoElement.removeContent(patternElement);
        int location;
        Element lookForElement = repoElement.getChild("includesPattern", ns);
        if (lookForElement != null) {
            location = repoElement.indexOf(lookForElement);
            repoElement.addContent(location + 1, patternElement);
            return;
        }
        lookForElement = repoElement.getChild("type", ns);
        if (lookForElement != null) {
            location = repoElement.indexOf(lookForElement);
            repoElement.addContent(location + 1, patternElement);
            return;
        }

        lookForElement = repoElement.getChild("description", ns);
        if (lookForElement != null) {
            location = repoElement.indexOf(lookForElement);
            repoElement.addContent(location + 1, patternElement);
            return;
        }

        lookForElement = repoElement.getChild("key", ns);
        location = repoElement.indexOf(lookForElement);
        repoElement.addContent(location + 1, patternElement);
    }
}

