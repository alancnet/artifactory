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

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * Defines the repository storage pattern
 *
 * @author Noam Y. Tenne
 */
@XmlType(name = "RepoLayoutType", propOrder = {"name", "artifactPathPattern", "distinctiveDescriptorPathPattern",
        "descriptorPathPattern", "folderIntegrationRevisionRegExp", "fileIntegrationRevisionRegExp"},
        namespace = Descriptor.NS)
public class RepoLayout implements Descriptor {

    @XmlID
    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String artifactPathPattern;

    @XmlElement(required = true)
    private boolean distinctiveDescriptorPathPattern;

    @XmlElement(required = false)
    private String descriptorPathPattern;

    @XmlElement(required = false)
    private String folderIntegrationRevisionRegExp;

    @XmlElement(required = false)
    private String fileIntegrationRevisionRegExp;

    public RepoLayout() {
    }

    public RepoLayout(RepoLayout copy) {
        this.name = copy.getName();
        this.artifactPathPattern = copy.getArtifactPathPattern();
        this.distinctiveDescriptorPathPattern = copy.isDistinctiveDescriptorPathPattern();
        this.descriptorPathPattern = copy.getDescriptorPathPattern();
        this.folderIntegrationRevisionRegExp = copy.getFolderIntegrationRevisionRegExp();
        this.fileIntegrationRevisionRegExp = copy.getFileIntegrationRevisionRegExp();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtifactPathPattern() {
        return artifactPathPattern;
    }

    public void setArtifactPathPattern(String artifactPathPattern) {
        this.artifactPathPattern = artifactPathPattern;
    }

    public boolean isDistinctiveDescriptorPathPattern() {
        return distinctiveDescriptorPathPattern;
    }

    public void setDistinctiveDescriptorPathPattern(boolean distinctiveDescriptorPathPattern) {
        this.distinctiveDescriptorPathPattern = distinctiveDescriptorPathPattern;
    }

    public String getDescriptorPathPattern() {
        return descriptorPathPattern;
    }

    public void setDescriptorPathPattern(String descriptorPathPattern) {
        this.descriptorPathPattern = descriptorPathPattern;
    }

    public String getFolderIntegrationRevisionRegExp() {
        return folderIntegrationRevisionRegExp;
    }

    public void setFolderIntegrationRevisionRegExp(String folderIntegrationRevisionRegExp) {
        this.folderIntegrationRevisionRegExp = folderIntegrationRevisionRegExp;
    }

    public String getFileIntegrationRevisionRegExp() {
        return fileIntegrationRevisionRegExp;
    }

    public void setFileIntegrationRevisionRegExp(String fileIntegrationRevisionRegExp) {
        this.fileIntegrationRevisionRegExp = fileIntegrationRevisionRegExp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RepoLayout)) {
            return false;
        }

        RepoLayout that = (RepoLayout) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

}
