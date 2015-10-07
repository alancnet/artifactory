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

package org.artifactory.descriptor.repo;

import java.io.Serializable;

/**
 * @author Noam Y. Tenne
 */
public class RepoLayoutBuilder implements Serializable {

    private String name;

    private String artifactPathPattern;

    private boolean distinctiveDescriptorPathPattern;

    private String descriptorPathPattern;

    private String folderIntegrationRevisionRegExp;

    private String fileIntegrationRevisionRegExp;

    public RepoLayoutBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RepoLayoutBuilder artifactPathPattern(String artifactPathPattern) {
        this.artifactPathPattern = artifactPathPattern;
        return this;
    }

    public RepoLayoutBuilder distinctiveDescriptorPathPattern(boolean distinctiveDescriptorPathPattern) {
        this.distinctiveDescriptorPathPattern = distinctiveDescriptorPathPattern;
        return this;
    }

    public RepoLayoutBuilder descriptorPathPattern(String descriptorPathPattern) {
        this.descriptorPathPattern = descriptorPathPattern;
        return this;
    }

    public RepoLayoutBuilder folderIntegrationRevisionRegExp(String folderIntegrationRevisionRegExp) {
        this.folderIntegrationRevisionRegExp = folderIntegrationRevisionRegExp;
        return this;
    }

    public RepoLayoutBuilder fileIntegrationRevisionRegExp(String fileIntegrationRevisionRegExp) {
        this.fileIntegrationRevisionRegExp = fileIntegrationRevisionRegExp;
        return this;
    }

    public RepoLayout build() {
        RepoLayout repoLayout = new RepoLayout();
        repoLayout.setName(name);
        repoLayout.setArtifactPathPattern(artifactPathPattern);
        repoLayout.setDistinctiveDescriptorPathPattern(distinctiveDescriptorPathPattern);
        repoLayout.setDescriptorPathPattern(descriptorPathPattern);
        repoLayout.setFolderIntegrationRevisionRegExp(folderIntegrationRevisionRegExp);
        repoLayout.setFileIntegrationRevisionRegExp(fileIntegrationRevisionRegExp);

        return repoLayout;
    }
}
