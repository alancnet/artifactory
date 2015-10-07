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

package org.artifactory.descriptor.repo;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolved recursively the search order of the virtual repositories. The resolving is done according to the virtual
 * repository repositories list order. Local repositories are always placed first. If the virtual repo has cycles (one
 * or more virtual repos appear more than once) the resolver will skip the repeated virtual repo.
 * <p><b>NOTE!</b> Do not remove the serializable interface since this class is being used inside a wicket page.
 *
 * @author Yossi Shaul
 */
public class VirtualRepoResolver implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(VirtualRepoResolver.class);

    private VirtualResolverFilter filter;

    private List<LocalRepoDescriptor> localRepos;
    private List<RemoteRepoDescriptor> remoteRepos;

    private boolean hasCycle = false;

    public VirtualRepoResolver(VirtualRepoDescriptor virtual) {
        filter = new EmptyVirtualResolverFilter();
        update(virtual);
    }

    public VirtualRepoResolver(VirtualRepoDescriptor virtual, VirtualResolverFilter filter) {
        this.filter = filter;
        update(virtual);
    }

    public void update(VirtualRepoDescriptor virtual) {
        localRepos = Lists.newArrayList();
        remoteRepos = Lists.newArrayList();
        hasCycle = false;
        resolve(virtual, new ArrayList<VirtualRepoDescriptor>());
    }

    private void resolve(VirtualRepoDescriptor virtualRepo, List<VirtualRepoDescriptor> visitedVirtualRepos) {
        if (visitedVirtualRepos.contains(virtualRepo)) {
            // don't visit twice the same virtual repo to prevent cycles
            log.debug("Virtual repo {} already visited.", visitedVirtualRepos);
            hasCycle = true;
            return;
        }

        // First filter the current virtual repo
        if (!filter.accepts(virtualRepo)) {
            return;
        }

        visitedVirtualRepos.add(virtualRepo);
        List<RepoDescriptor> repos = virtualRepo.getRepositories();
        for (RepoDescriptor repo : repos) {
            if (repo instanceof LocalRepoDescriptor) {
                LocalRepoDescriptor localRepo = (LocalRepoDescriptor) repo;
                if (!localRepos.contains(localRepo)) {
                    localRepos.add(localRepo);
                }
            } else if (repo instanceof RemoteRepoDescriptor) {
                RemoteRepoDescriptor remoteRepo = (RemoteRepoDescriptor) repo;
                if (!remoteRepos.contains(remoteRepo)) {
                    remoteRepos.add(remoteRepo);
                }
            } else if (repo instanceof VirtualRepoDescriptor) {
                // resolve recursively
                VirtualRepoDescriptor virtualRepoDescriptor = (VirtualRepoDescriptor) repo;
                if (filter.accepts(virtualRepoDescriptor)) {
                    resolve(virtualRepoDescriptor, visitedVirtualRepos);
                }
            } else {
                log.warn("Unexpected repository of type '{}'.", repo.getClass());
            }
        }
    }

    /**
     * @return List of all the detected local repositories.
     */
    public List<LocalRepoDescriptor> getLocalRepos() {
        return localRepos;
    }

    /**
     * @return List of all the detected remote repositories.
     */
    public List<RemoteRepoDescriptor> getRemoteRepos() {
        return remoteRepos;
    }

    /**
     * @return List with all the resolved local and remote repositories ordered correctly.
     */
    public List<RealRepoDescriptor> getOrderedRepos() {
        List<RealRepoDescriptor> orderedRepos =
                new ArrayList<>(localRepos.size() + remoteRepos.size());
        orderedRepos.addAll(localRepos);
        orderedRepos.addAll(remoteRepos);
        return orderedRepos;
    }

    /**
     * @return True if the virtual repository contains a cycle (virtual repo that appears more than once).
     */
    public boolean hasCycle() {
        return hasCycle;
    }

    /**
     * Returns a boolean value which represents if the given repo descriptor is associated with the virtual repo which
     * has been resolved. Please note, that the current version of the resolver does not support caches. To check if a
     * cache is associated, Supply it's remote repo instead.
     *
     * @param descriptor A repository descriptor
     * @return boolean - True if the supplied repo is associated with the resolved virtual repo. False if not.
     */
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public boolean contains(RepoDescriptor descriptor) {
        boolean contains = false;
        if (descriptor instanceof LocalRepoDescriptor) {
            contains = localRepos.contains(descriptor);
        } else if (descriptor instanceof RemoteRepoDescriptor) {
            contains = remoteRepos.contains(descriptor);
        }
        return contains;
    }
}
