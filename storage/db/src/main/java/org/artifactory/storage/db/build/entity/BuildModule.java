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

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * Date: 10/30/12
 * Time: 12:19 PM
 *
 * @author freds
 */
public class BuildModule {
    private final long moduleId;
    private final long buildId;
    private final String moduleNameId;

    /**
     * Initialized as null, and can (and should) be set only once
     */
    private ImmutableSet<ModuleProperty> properties = null;

    public BuildModule(long moduleId, long buildId, String moduleNameId) {
        if (moduleId <= 0L || buildId <= 0L) {
            throw new IllegalArgumentException("Module or Build id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(moduleNameId)) {
            throw new IllegalArgumentException("Module id name cannot be null!");
        }
        this.moduleId = moduleId;
        this.buildId = buildId;
        this.moduleNameId = moduleNameId;
    }

    public long getModuleId() {
        return moduleId;
    }

    public long getBuildId() {
        return buildId;
    }

    public String getModuleNameId() {
        return moduleNameId;
    }

    public ImmutableSet<ModuleProperty> getProperties() {
        if (properties == null) {
            throw new IllegalStateException("Build Module object was not initialized correctly! Properties missing.");
        }
        return properties;
    }

    public void setProperties(Set<ModuleProperty> properties) {
        if (this.properties != null) {
            throw new IllegalStateException("Cannot set Properties already set!");
        }
        if (properties == null) {
            throw new IllegalArgumentException("Cannot set properties to null");
        }
        this.properties = ImmutableSet.copyOf(properties);
    }
}
