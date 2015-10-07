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
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public class SingleRepoTypeConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(SingleRepoTypeConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Converting repositories to a single package type");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element localRepos = rootElement.getChild("localRepositories", namespace);
        if (localRepos != null) {
            convertLocalRepos(localRepos.getChildren());
        }

        Element remoteRepos = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepos != null) {
            convertRemoteRepos(remoteRepos.getChildren());
        }

        Element virtualRepos = rootElement.getChild("virtualRepositories", namespace);
        if (virtualRepos != null) {
            convertVirtualRepos(virtualRepos.getChildren());
        }

        log.info("Finished Converting repositories to a single package type");
    }

    private void convertLocalRepos(List<Element> repos) {
        if (repos == null || repos.isEmpty()) {
            return;
        }

        for (Element repo : repos) {
            List<RepoType> repoTypes = Lists.newArrayList();
            fillSharedTypes(repoTypes, repo, false);
            addType(repoTypes, repo, RepoType.YUM, false);
            String repoKey = repo.getChildText("key", repo.getNamespace());
            convertToSingleRepoType(repo, repoKey, repoTypes, false);
        }

    }

    private void convertRemoteRepos(List<Element> repos) {
        if (repos == null || repos.isEmpty()) {
            return;
        }

        for (Element repo : repos) {
            List<RepoType> repoTypes = Lists.newArrayList();
            fillSharedTypes(repoTypes, repo, false);
            if (!repoTypes.contains(RepoType.Bower)) {
                // Bower + VCS = Bower repository
                addType(repoTypes, repo, RepoType.VCS, false);
            }
            String repoKey = repo.getChildText("key", repo.getNamespace());
            convertToSingleRepoType(repo, repoKey, repoTypes, false);
        }
    }

    private void convertVirtualRepos(List<Element> repos) {
        if (repos == null || repos.isEmpty()) {
            return;
        }

        for (Element repo : repos) {
            List<RepoType> repoTypes = Lists.newArrayList();
            fillSharedTypes(repoTypes, repo, true);
            Element p2 = repo.getChild("p2", repo.getNamespace());
            if (p2 != null) {
                if (StringUtils.equals(p2.getChildText("enabled", p2.getNamespace()), "true")) {
                    addType(repoTypes, p2, RepoType.P2, true);
                }
            }

            String repoKey = repo.getChildText("key", repo.getNamespace());
            convertToSingleRepoType(repo, repoKey, repoTypes, true);
        }
    }

    private void convertToSingleRepoType(Element repo, String repoKey, List<RepoType> repoTypes, boolean virtualRepo) {
        if (repoTypes.size() >= 1) {
            repoTypes
                    .stream()
                    .skip(1) // First one is used as the final repository type
                    .forEach(repoType -> log.error("Disabling package '{}' for repo '{}' " +
                            "since only one packaging type is allowed!", repoType, repoKey));
        } else {
            String layoutRef = repo.getChildText("repoLayoutRef", repo.getNamespace());
            if (StringUtils.equals(layoutRef, "ivy-default")) {
                repoTypes.add(RepoType.Ivy);
            } else if (StringUtils.equals(layoutRef, "gradle-default")) {
                repoTypes.add(RepoType.Gradle);
            } else {
                repoTypes.add(RepoType.Maven);
            }
        }

        // Set the final decided repo type
        RepoType repoType = repoTypes.get(0);
        log.info("Setting repository '{}' to type {}", repoKey, repoType);
        Element typeElement = new Element("type", repo.getNamespace());
        typeElement.setText(String.valueOf(repoType).toLowerCase());
        repo.addContent(2, new Text("\n            "));
        repo.addContent(3, typeElement); // add the type after the key property
        removeEnabledFieldsFromRepo(repo, virtualRepo);
    }

    private void removeEnabledFieldsFromRepo(Element repo, boolean virtualRepo) {
        for (RepoType repoType : RepoType.values()) {
            if (repoType.equals(RepoType.P2)) {
                Element p2 = repo.getChild("p2", repo.getNamespace());
                if (p2 != null) {
                    if (p2.getChild("enabled", p2.getNamespace()) != null) {
                        p2.removeChild("enabled", p2.getNamespace());
                    }
                }
            } else if (!repoType.equals(RepoType.YUM)) {
                //Special handle for YUM config as a repo can still be chosen to be of YUM type but with auto-calc off
                String field = resolveFieldName(repoType, virtualRepo);
                if (repo.getChild(field, repo.getNamespace()) != null) {
                    repo.removeChild(field, repo.getNamespace());
                }
            } else {
                // If the repo type is YUM then we shouldn't remove any field
            }
        }
    }

    private String resolveFieldName(RepoType repoType, boolean virtualRepo) {
        if (virtualRepo) {
            return repoType.getVirtualField();
        } else {
            return repoType.getLocalAndRemoteField();
        }
    }

    private void fillSharedTypes(List<RepoType> repoTypes, Element element, boolean virtualRepo) {
        addType(repoTypes, element, RepoType.NuGet, virtualRepo);
        addType(repoTypes, element, RepoType.Gems, virtualRepo);
        addType(repoTypes, element, RepoType.Npm, virtualRepo);
        addType(repoTypes, element, RepoType.Bower, virtualRepo);
        addType(repoTypes, element, RepoType.Debian, virtualRepo);
        addType(repoTypes, element, RepoType.Pypi, virtualRepo);
        addType(repoTypes, element, RepoType.Docker, virtualRepo);
        addType(repoTypes, element, RepoType.Vagrant, virtualRepo);
        addType(repoTypes, element, RepoType.GitLfs, virtualRepo);
        addType(repoTypes, element, RepoType.P2, virtualRepo);
        addType(repoTypes, element, RepoType.YUM, virtualRepo);
        if (!virtualRepo && !repoTypes.contains(RepoType.YUM)) {
            addYumForLegacy(repoTypes, element);
        }
    }

    private void addYumForLegacy(List<RepoType> repoTypes, Element element) {
        String depth = element.getChildText("yumRootDepth", element.getNamespace());
        String groupNames = element.getChildText("yumGroupFileNames", element.getNamespace());
        try {
            if ((StringUtils.isNotBlank(depth) && Integer.valueOf(depth) > 0) || StringUtils.isNotBlank(groupNames)) {
                repoTypes.add(RepoType.YUM);
            }
        } catch (NumberFormatException e) {
            log.warn("Unresolvable YUM configuration, YUM metadata root depth is {}. ", depth);
        }
    }

    private void addType(List<RepoType> repoTypes, Element element, RepoType type, boolean virtualRepo) {
        String fieldName = resolveFieldName(type, virtualRepo);
        boolean isEnabled = Boolean.valueOf(element.getChildText(fieldName, element.getNamespace()));
        if (isEnabled) {
            repoTypes.add(type);
        }
    }

private enum RepoType {
    NuGet("enableNuGetSupport", "enableNuGetSupport"),
    Gems("enableGemsSupport", "enableGemsSupport"),
    Npm("enableNpmSupport", "enableNpmSupport"),
    Bower("enableBowerSupport", "enableBowerSupport"),
    Debian("enableDebianSupport", "enableDebianSupport"),
    Pypi("enablePypiSupport", "enablePypiSupport"),
    Docker("enableDockerSupport", "enableDockerSupport"),
    Vagrant("enableVagrantSupport", "enableVagrantSupport"),
    GitLfs("enableGitLfsSupport", "enableGitLfsSupport"),
    YUM("calculateYumMetadata", "calculateYumMetadata"),
    VCS("enableVcsSupport", "enableVcsSupport"),
    P2("p2Support", "enabled"),
    Maven("", ""),
    Gradle("", ""),
    Ivy("", ""),
    Generic("", "");

    private String localAndRemoteField;
    private String virtualField;

    RepoType(String localAndRemoteField, String virtualField) {
        this.virtualField = virtualField;
        this.localAndRemoteField = localAndRemoteField;
    }

    public String getLocalAndRemoteField() {
        return localAndRemoteField;
    }

    public String getVirtualField() {
        return virtualField;
    }

    public boolean isMavenGroup() {
        return this == Maven || this == Ivy || this == Gradle || this == P2;
    }
}
}
