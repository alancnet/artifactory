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

package org.artifactory.repo.virtual.interceptor;

import org.apache.http.HttpStatus;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.InternalRequestContext;
import org.artifactory.resource.UnfoundRepoResource;

import java.util.List;

/**
 * Base/Adapter implementation for {@link VirtualRepoInterceptor} implementations.
 *
 * @author Yossi Shaul
 */
public abstract class VirtualRepoInterceptorBase implements VirtualRepoInterceptor {

    @Override
    public RepoResource onBeforeReturn(VirtualRepo virtualRepo, InternalRequestContext context, RepoResource resource) {
        return resource;
    }

    @Override
    public RepoResource interceptGetInfo(VirtualRepo virtualRepo, InternalRequestContext context, RepoPath repoPath,
            List<RealRepo> repositories) {
        return null;
    }

    /**
     * @param resource Resource to check
     * @return Returns the given resource iff the status code is forbidden (403). Otherwise returns null
     */
    public static UnfoundRepoResource checkIfForbidden(RepoResource resource) {
        if (resource instanceof UnfoundRepoResource) {
            if (((UnfoundRepoResource) resource).getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                return (UnfoundRepoResource) resource;
            }
        }
        return null;
    }
}
