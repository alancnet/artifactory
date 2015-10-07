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

package org.artifactory.repo.virtual;

import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualResolverFilter;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.RepoRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual resolver filter which filters virtual repo according to a request path and include/exclude pattern.
 *
 * @author Shay Yaakov
 */
public class VirtualResolverRequestFilter implements VirtualResolverFilter {
    private static final Logger log = LoggerFactory.getLogger(VirtualResolverRequestFilter.class);

    private VirtualRepo virtualRepo;
    private RepoPath repoPath;
    private InternalRepositoryService repositoryService;
    private LayoutsCoreAddon layoutsCoreAddon;

    public VirtualResolverRequestFilter(VirtualRepo virtualRepo, RepoPath repoPath,
            InternalRepositoryService repositoryService, LayoutsCoreAddon layoutsCoreAddon) {
        this.virtualRepo = virtualRepo;
        this.repoPath = repoPath;
        this.repositoryService = repositoryService;
        this.layoutsCoreAddon = layoutsCoreAddon;
    }

    @Override
    public boolean accepts(VirtualRepoDescriptor childDescriptor) {
        String childVirtualRepoKey = childDescriptor.getKey();
        VirtualRepo childVirtualRepo = repositoryService.virtualRepositoryByKey(childVirtualRepoKey);
        if (childVirtualRepo == null) {
            log.error("Could not find virtual repository with key '{}'", childVirtualRepoKey);
            return false;
        }

        String path = repoPath.getPath();
        String translatedPath = translateRepoPath(virtualRepo.getDescriptor(), childDescriptor, path);
        if (!translatedPath.equals(path)) {
            RepoRequests.logToContext("Resource was translated to '%s' in order to search within '%s'",
                    translatedPath, childVirtualRepoKey);
        }
        RepoPath translatedRepoPath = InternalRepoPathFactory.create(childVirtualRepoKey, translatedPath,
                repoPath.isFolder());
        if (!childVirtualRepo.accepts(translatedRepoPath)) {
            // includes/excludes should not affect system paths
            RepoRequests.logToContext("Adding no aggregated repositories - requested artifact is rejected by the " +
                    "include exclude patterns of '%s'", childVirtualRepoKey);
            return false;
        }

        RepoRequests.logToContext("Appending the virtual repository '%s'", virtualRepo.getKey());
        return true;
    }

    private String translateRepoPath(VirtualRepoDescriptor source, VirtualRepoDescriptor target, String path) {
        RepoLayout sourceRepoLayout = source.getRepoLayout();
        RepoLayout targetRepoLayout = target.getRepoLayout();

        return layoutsCoreAddon.translateArtifactPath(sourceRepoLayout, targetRepoLayout, path);
    }
}
