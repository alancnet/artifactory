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

package org.artifactory.repo;

import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;

/**
 * Interface for the local cache repositories.
 *
 * @author Noam Tenne
 */
public interface LocalCacheRepo extends LocalRepo<LocalCacheRepoDescriptor> {

    RemoteRepo<? extends RemoteRepoDescriptor> getRemoteRepo();

    RemoteRepoDescriptor getRemoteRepoDescriptor();

    void unexpire(String path);

    /**
     * @see org.artifactory.api.repo.RepositoryService#zap(org.artifactory.api.repo.RepoPath)
     */
    int zap(RepoPath repoPath);
}
