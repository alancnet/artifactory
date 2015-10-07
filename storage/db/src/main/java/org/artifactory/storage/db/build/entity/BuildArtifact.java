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

package org.artifactory.storage.db.build.entity;

import org.apache.commons.lang.StringUtils;

/**
 * Date: 10/30/12
 * Time: 12:05 PM
 *
 * @author freds
 */
public class BuildArtifact {
    private final long artifactId;
    private final long moduleId;
    private final String artifactName;
    private final String artifactType;
    private final String sha1;
    private final String md5;

    public BuildArtifact(long artifactId, long moduleId, String artifactName, String artifactType, String sha1,
            String md5) {
        if (artifactId <= 0L || moduleId <= 0L) {
            throw new IllegalArgumentException("Artifact or Module id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(artifactName)) {
            throw new IllegalArgumentException("Artifact name cannot be null!");
        }
        this.artifactId = artifactId;
        this.moduleId = moduleId;
        this.artifactName = artifactName;
        this.artifactType = artifactType;
        this.sha1 = sha1;
        this.md5 = md5;
    }

    public long getArtifactId() {
        return artifactId;
    }

    public long getModuleId() {
        return moduleId;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public String getSha1() {
        return sha1;
    }

    public String getMd5() {
        return md5;
    }

    @Override
    public String toString() {
        return "BuildArtifact{" +
                "artifactId=" + artifactId +
                ", moduleId=" + moduleId +
                ", artifactName='" + artifactName + '\'' +
                ", artifactType='" + artifactType + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
