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

package org.artifactory.api.build;

import com.google.common.collect.Sets;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumsInfo;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
@XStreamAlias("exported-build")
public class ImportableExportableBuild implements Serializable {
    @XStreamAsAttribute
    private String version;

    private String buildName;
    private String buildNumber;
    private String buildStarted;
    private String json;
    private Set<String> artifactChecksums;
    private Set<String> dependencyChecksums;

    private long created;
    private long lastModified;
    private String createdBy;
    private String lastModifiedBy;
    private String mimeType;
    private ChecksumsInfo checksumsInfo;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildStarted() {
        return buildStarted;
    }

    public void setBuildStarted(String buildStarted) {
        this.buildStarted = buildStarted;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Set<String> getArtifactChecksums() {
        return artifactChecksums;
    }

    public void addArtifactChecksum(String artifactChecksum) {
        if (artifactChecksums == null) {
            artifactChecksums = Sets.newHashSet();
        }
        artifactChecksums.add(artifactChecksum);
    }

    public void setArtifactChecksums(Set<String> artifactChecksums) {
        this.artifactChecksums = artifactChecksums;
    }

    public Set<String> getDependencyChecksums() {
        return dependencyChecksums;
    }

    public void addDependencyChecksum(String dependencyChecksum) {
        if (dependencyChecksums == null) {
            dependencyChecksums = Sets.newHashSet();
        }
        dependencyChecksums.add(dependencyChecksum);
    }

    public void setDependencyChecksums(Set<String> dependencyChecksums) {
        this.dependencyChecksums = dependencyChecksums;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ChecksumsInfo getChecksumsInfo() {
        return checksumsInfo;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void addChecksumInfo(ChecksumInfo checksumInfo) {
        if (checksumsInfo == null) {
            checksumsInfo = new ChecksumsInfo();
        }
        checksumsInfo.addChecksumInfo(checksumInfo);
    }

    public void setChecksumsInfo(ChecksumsInfo checksumsInfo) {
        this.checksumsInfo = checksumsInfo;
    }
}
