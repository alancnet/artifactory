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

package org.artifactory.storage.db.security.entity;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.artifactory.util.PathUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Date: 9/3/12
 * Time: 12:24 PM
 *
 * @author freds
 */
public class PermissionTarget {
    private static final String DELIMITER = ",";

    private final long permTargetId;
    private final String name;
    private final List<String> includes;
    private final List<String> excludes;

    /**
     * Initialized as null, and can (and should) be set only once
     */
    private ImmutableSet<String> repoKeys = null;

    public PermissionTarget(long id, String name, List<String> includes, List<String> excludes) {
        if (id <= 0L) {
            throw new IllegalArgumentException("Permission target id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Permission target name cannot be null!");
        }
        this.permTargetId = id;
        this.name = name;
        this.includes = includes == null ? Collections.<String>emptyList() : includes;
        this.excludes = excludes == null ? Collections.<String>emptyList() : excludes;
    }

    public PermissionTarget(long id, String name, String includes, String excludes) {
        this(id, name,
                PathUtils.includesExcludesPatternToStringList(includes),
                PathUtils.includesExcludesPatternToStringList(excludes));
    }

    public long getPermTargetId() {
        return permTargetId;
    }

    public String getName() {
        return name;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public String getIncludesPattern() {
        if (includes == null || includes.isEmpty()) {
            return null;
        }
        return PathUtils.collectionToDelimitedString(includes, DELIMITER);
    }

    public String getExcludesPattern() {
        if (excludes == null || excludes.isEmpty()) {
            return null;
        }
        return PathUtils.collectionToDelimitedString(excludes, DELIMITER);
    }

    public Collection<String> getRepoKeys() {
        if (repoKeys == null) {
            throw new IllegalStateException(
                    "Permission Target object was not initialized correctly! List of repo keys missing.");
        }
        return repoKeys;
    }

    public void setRepoKeys(Set<String> repoKeys) {
        if (this.repoKeys != null) {
            throw new IllegalStateException("Cannot set repository keys already set!");
        }
        if (repoKeys == null) {
            throw new IllegalArgumentException("Cannot set repository keys to null");
        }
        this.repoKeys = ImmutableSet.copyOf(repoKeys);
    }

    public boolean isIdentical(PermissionTarget pt) {
        if (pt == this) {
            return true;
        }
        if (pt == null) {
            return false;
        }
        if (pt.permTargetId != this.permTargetId
                || !StringUtils.equals(pt.name, this.name)
                || !Objects.equal(pt.includes, this.includes)
                || !Objects.equal(pt.excludes, this.excludes)
                || !Objects.equal(pt.repoKeys, this.repoKeys)
                ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PermissionTarget{" +
                "permTargetId=" + permTargetId +
                ", name='" + name + '\'' +
                ", includes=" + includes +
                ", excludes=" + excludes +
                ", repoKeys=" + repoKeys +
                '}';
    }
}
