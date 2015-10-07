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


import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * A build module holder. Contains artifacts and dependencies.
 *
 * @author Noam Y. Tenne
 */
public final class Module implements Serializable {

    private org.jfrog.build.api.Module module;

    public Module(@Nonnull org.jfrog.build.api.Module module) {
        this.module = module;
    }

    public String getId() {
        return module.getId();
    }

    public void setId(String id) {
        module.setId(id);
    }

    @Nonnull
    public List<Artifact> getArtifacts() {
        List<org.jfrog.build.api.Artifact> moduleArtifacts = module.getArtifacts();
        if (moduleArtifacts == null) {
            moduleArtifacts = Lists.newArrayList();
            module.setArtifacts(moduleArtifacts);
        }
        return new ArtifactList(moduleArtifacts);
    }

    @Nonnull
    public List<Dependency> getDependencies() {
        List<org.jfrog.build.api.Dependency> moduleDependencies = module.getDependencies();
        if (moduleDependencies == null) {
            moduleDependencies = Lists.newArrayList();
            module.setDependencies(moduleDependencies);
        }
        return new DependencyList(moduleDependencies);
    }
}