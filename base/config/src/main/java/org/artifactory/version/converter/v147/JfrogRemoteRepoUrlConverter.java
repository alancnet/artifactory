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

package org.artifactory.version.converter.v147;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Converter which fixes the JFrog libs and JFrog plugins URLs that came bundled in Artifactory 2.3.1 that are pointing
 * to the wrong URL on repo.jfrog.org
 *
 * @author Tomer Cohen
 */
public class JfrogRemoteRepoUrlConverter implements XmlConverter {

    private static final String OLD_JFROG_LIBS = "http://repo.jfrog.org/artifactory/libs-release-local";
    private static final String NEW_JFROG_LIBS = "http://repo.jfrog.org/artifactory/libs-releases-local";
    private static final String OLD_JFROG_PLUGINS = "http://repo.jfrog.org/artifactory/plugins-release-local";
    private static final String NEW_JFROG_PLUGINS = "http://repo.jfrog.org/artifactory/plugins-releases-local";

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element repositories = rootElement.getChild("remoteRepositories", namespace);
        if (repositories != null) {
            List remoteRepos = repositories.getChildren("remoteRepository", namespace);
            if (remoteRepos != null && !remoteRepos.isEmpty()) {
                for (Object remoteRepo : remoteRepos) {
                    Element remoteRepoElement = (Element) remoteRepo;
                    Element url = remoteRepoElement.getChild("url", namespace);
                    if (url != null) {
                        String urlText = url.getText();
                        if (OLD_JFROG_LIBS.equals(urlText)) {
                            url.setText(NEW_JFROG_LIBS);
                        } else if (OLD_JFROG_PLUGINS.equals(urlText)) {
                            url.setText(NEW_JFROG_PLUGINS);
                        }
                    }
                }
            }
        }
    }
}
