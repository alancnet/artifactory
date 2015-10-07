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

package org.artifactory.build;

import org.apache.commons.lang.StringUtils;
import org.artifactory.fs.FileInfo;
import org.artifactory.util.PathUtils;
import org.jfrog.build.api.builder.DependencyBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * A basic build run info holder
 *
 * @author Noam Y. Tenne
 */
public final class Dependency extends BuildFile {

    private org.jfrog.build.api.Dependency dependency;

    Dependency(@Nonnull org.jfrog.build.api.Dependency dependency) {
        super(dependency);
        this.dependency = dependency;
    }

    public Dependency(@Nonnull String id, @Nonnull FileInfo fileInfo, @Nonnull Set<String> scopes,
            @Nullable String type) {
        this(new DependencyBuilder().id(id).md5(fileInfo.getMd5()).sha1(fileInfo.getSha1()).scopes(scopes)
                .type(StringUtils.isNotBlank(type) ? type : PathUtils.getExtension(fileInfo.getName())).build());
    }

    public String getId() {
        return dependency.getId();
    }

    public Set<String> getScopes() {
        return dependency.getScopes();
    }

    @Nonnull
    org.jfrog.build.api.Dependency getBuildDependency() {
        return dependency;
    }
}