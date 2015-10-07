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
import org.artifactory.util.PathUtils;

import java.util.List;
import java.util.Set;

/**
 * Date: 10/30/12
 * Time: 12:05 PM
 *
 * @author freds
 */
public class BuildDependency {
    private static final String DELIMITER = ",";

    private final long dependencyId;
    private final long moduleId;
    private final String dependencyNameId;
    private final String dependencyScopes;
    private final String dependencyType;
    private final String sha1;
    private final String md5;

    public BuildDependency(long dependencyId, long moduleId, String dependencyNameId, Set<String> dependencyScopes,
            String dependencyType, String sha1, String md5) {
        this(dependencyId, moduleId, dependencyNameId,
                PathUtils.collectionToDelimitedString(dependencyScopes, DELIMITER), dependencyType, sha1, md5);
    }

    public BuildDependency(long dependencyId, long moduleId, String dependencyNameId, String dependencyScopes,
            String dependencyType, String sha1, String md5) {
        if (dependencyId <= 0L || moduleId <= 0L) {
            throw new IllegalArgumentException("Dependency or Module id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(dependencyNameId)) {
            throw new IllegalArgumentException("Dependency id name cannot be null!");
        }
        this.dependencyId = dependencyId;
        this.moduleId = moduleId;
        this.dependencyNameId = dependencyNameId;
        this.dependencyScopes = dependencyScopes;
        this.dependencyType = dependencyType;
        this.sha1 = sha1;
        this.md5 = md5;
    }

    public long getDependencyId() {
        return dependencyId;
    }

    public long getModuleId() {
        return moduleId;
    }

    public String getDependencyNameId() {
        return dependencyNameId;
    }

    public String getDependencyScopes() {
        return dependencyScopes;
    }

    public List<String> getScopes() {
        return PathUtils.includesExcludesPatternToStringList(dependencyScopes);
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public String getSha1() {
        return sha1;
    }

    public String getMd5() {
        return md5;
    }

    @Override
    public String toString() {
        return "BuildDependency{" +
                "dependencyId=" + dependencyId +
                ", moduleId=" + moduleId +
                ", dependencyNameId='" + dependencyNameId + '\'' +
                ", dependencyScopes='" + dependencyScopes + '\'' +
                ", dependencyType='" + dependencyType + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
