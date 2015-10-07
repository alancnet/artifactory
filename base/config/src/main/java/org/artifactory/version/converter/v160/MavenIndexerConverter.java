/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.version.converter.v160;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert the maven indexer from excluded repositories to hold included repositories
 *
 * @author Shay Yaakov
 */
public class MavenIndexerConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(MavenIndexerConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Converting maven indexer to included repositories");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element indexer = rootElement.getChild("indexer", namespace);
        if (indexer != null) {
            Element excludedRepositories = indexer.getChild("excludedRepositories", namespace);
            if (excludedRepositories != null) {
                replaceExcludedWithIncluded(rootElement, namespace, indexer, excludedRepositories);
            }
        }

        log.info("Finished converting maven indexer to included repositories");
    }

    private void replaceExcludedWithIncluded(Element rootElement, Namespace namespace, Element indexer,
            Element excludedRepositories) {
        List<String> excluded = excludedRepositories.getChildren()
                .stream()
                .map(Element::getText)
                .collect(Collectors.toList());


        if (StringUtils.equals(indexer.getChildText("enabled", namespace), "true")) {
            Element includedRepositories = new Element("includedRepositories", namespace);
            collectRepositories(rootElement, namespace)
                    .stream()
                    .filter(repo -> !excluded.contains(repo))
                    .forEach(repo -> {
                        Element repositoryRef = new Element("repositoryRef", namespace);
                        repositoryRef.setText(repo);
                        includedRepositories.addContent(repositoryRef);
                    });
            indexer.addContent(new Text("\n        "));
            indexer.addContent(includedRepositories);
        }

        indexer.removeContent(excludedRepositories);
    }

    private List<String> collectRepositories(Element rootElement, Namespace namespace) {
        List<String> repoKeys = Lists.newArrayList();

        Element localRepos = rootElement.getChild("localRepositories", namespace);
        collectRepoKeys(repoKeys, namespace, localRepos);

        Element remoteRepos = rootElement.getChild("remoteRepositories", namespace);
        collectRepoKeys(repoKeys, namespace, remoteRepos);

        Element virtualRepos = rootElement.getChild("virtualRepositories", namespace);
        collectRepoKeys(repoKeys, namespace, virtualRepos);

        return repoKeys;
    }

    private void collectRepoKeys(List<String> repoKeys, Namespace namespace, Element repos) {
        if (repos != null) {
            List<Element> children = repos.getChildren();
            if (children != null) {
                children
                        .stream()
                        .filter(element -> {
                            boolean isMavenLayout = StringUtils.equals("maven-2-default",
                                    element.getChildText("repoLayoutRef", namespace));
                            boolean isMavenType = RepoType.fromType(element.getChildText("type", namespace)).isMavenGroup();
                            return isMavenLayout && isMavenType;
                        })
                        .forEach(element -> repoKeys.add(element.getChildText("key", namespace)));
            }
        }
    }
}
