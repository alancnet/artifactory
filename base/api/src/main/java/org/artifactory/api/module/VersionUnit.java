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

package org.artifactory.api.module;

import com.google.common.collect.Sets;
import org.artifactory.repo.RepoPath;

import java.io.Serializable;
import java.util.Set;

/**
 * Version unit represents a group of deployed files. For maven it is the version path.
 *
 * @author Yossi Shaul
 */
public class VersionUnit implements Serializable {

    private final ModuleInfo moduleInfo;
    private final Set<RepoPath> repoPaths;
    private final Set<RepoPath> parents;

    public VersionUnit(ModuleInfo moduleInfo, Set<RepoPath> repoPaths) {
        this.moduleInfo = moduleInfo;
        this.repoPaths = repoPaths;
        parents = getParents(repoPaths);
    }

    private Set<RepoPath> getParents(Set<RepoPath> repoPaths) {
        Set<RepoPath> parentSet = Sets.newHashSet();
        for (RepoPath repoPath : repoPaths) {
            parentSet.add(repoPath.getParent());
        }

        return parentSet;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public Set<RepoPath> getRepoPaths() {
        return repoPaths;
    }

    public Set<RepoPath> getParents() {
        return parents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VersionUnit)) {
            return false;
        }

        VersionUnit that = (VersionUnit) o;

        if (moduleInfo != null ? !moduleInfo.equals(that.moduleInfo) : that.moduleInfo != null) {
            return false;
        }
        if (repoPaths != null ? !repoPaths.equals(that.repoPaths) : that.repoPaths != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = moduleInfo != null ? moduleInfo.hashCode() : 0;
        result = 31 * result + (repoPaths != null ? repoPaths.hashCode() : 0);
        return result;
    }
}
