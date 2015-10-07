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

package org.artifactory.build.staging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * @author Noam Y. Tenne
 */
public class ModuleVersion implements Serializable {

    private String moduleId;
    private String nextRelease;
    private String nextDevelopment;

    public ModuleVersion(@Nonnull String moduleId, @Nonnull String nextRelease, @Nonnull String nextDevelopment) {
        this.moduleId = moduleId;
        this.nextRelease = nextRelease;
        this.nextDevelopment = nextDevelopment;
    }

    public ModuleVersion() {
    }

    @Nullable
    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(@Nonnull String moduleId) {
        this.moduleId = moduleId;
    }

    @Nullable
    public String getNextRelease() {
        return nextRelease;
    }

    public void setNextRelease(@Nonnull String nextRelease) {
        this.nextRelease = nextRelease;
    }

    @Nullable
    public String getNextDevelopment() {
        return nextDevelopment;
    }

    public void setNextDevelopment(@Nonnull String nextDevelopment) {
        this.nextDevelopment = nextDevelopment;
    }
}
