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
 * Time: 2:45 PM
 *
 * @author freds
 */
public class ModuleProperty {
    private final long propId;
    private final long moduleId;
    private final String propKey;
    private final String propValue;

    public ModuleProperty(long propId, long moduleId, String propKey, String propValue) {
        if (propId <= 0L || moduleId <= 0L) {
            throw new IllegalArgumentException("Property or Module id cannot be zero or negative!");
        }
        if (StringUtils.isBlank(propKey)) {
            throw new IllegalArgumentException("Property key cannot be null!");
        }
        this.propId = propId;
        this.moduleId = moduleId;
        this.propKey = propKey;
        this.propValue = propValue;
    }

    public long getPropId() {
        return propId;
    }

    public long getModuleId() {
        return moduleId;
    }

    public String getPropKey() {
        return propKey;
    }

    public String getPropValue() {
        return propValue;
    }

    public boolean isIdentical(ModuleProperty mp) {
        if (this == mp) {
            return true;
        }
        if (mp == null || getClass() != mp.getClass()) {
            return false;
        }
        return moduleId == mp.moduleId
                && StringUtils.equals(propKey, mp.propKey)
                && StringUtils.equals(propValue, mp.propValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleProperty that = (ModuleProperty) o;

        if (propId != that.propId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (propId ^ (propId >>> 32));
    }

    @Override
    public String toString() {
        return "ModuleProperty{" +
                "propId=" + propId +
                ", moduleId=" + moduleId +
                ", propKey='" + propKey + '\'' +
                ", propValue='" + propValue + '\'' +
                '}';
    }
}
