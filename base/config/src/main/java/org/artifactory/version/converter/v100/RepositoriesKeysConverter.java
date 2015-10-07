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

package org.artifactory.version.converter.v100;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts repositories key values. This converter is neccessary for repository keys starting with numbers since
 * starting with 1.1.0 the repo keys are xml ids. The keys to replace should pass as system properties and filled in
 * ArtifactoryConstants.
 *
 * @author Yossi Shaul
 */
public class RepositoriesKeysConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(RepositoriesKeysConverter.class);

    @Override
    @SuppressWarnings({"unchecked"})
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        // get all local repositories
        Element localReposWrapper = root.getChild("localRepositories", ns);
        List localRepos = localReposWrapper.getChildren();
        List repos = new ArrayList(localRepos);

        // and add all remote repositories if any
        Element remoteReposWrapper = root.getChild("remoteRepositories", ns);
        if (remoteReposWrapper != null) {
            List remoteRepos = remoteReposWrapper.getChildren();
            if (remoteRepos != null) {
                repos.addAll(remoteRepos);
            }
        }

        for (Object repo : repos) {
            Element localRepo = (Element) repo;
            Element keyElement = localRepo.getChild("key", ns);
            String key = keyElement.getText();
            Map<String, String> keys = ArtifactoryHome.get().getArtifactoryProperties().getSubstituteRepoKeys();
            if (keys.containsKey(key)) {
                String newKey = keys.get(key);
                log.debug("Changing repository key from {} to {}", key, newKey);
                keyElement.setText(newKey);
            }
        }
    }
}
