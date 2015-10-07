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

import org.artifactory.fs.RepoResource;
import org.artifactory.repo.RealRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.request.InternalRequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for interceptors participating in the virtual repository download process.
 *
 * @author Yossi Shaul
 */
public interface VirtualRepoInterceptor {

    /**
     * Called before the main virtual repo downloading strategy is handling get info requests. Interceptors that do not
     * handle requests for this resource should return null. A returned non-null repo resource will be the result sent
     * to the client.
     *
     * @param virtualRepo  The virtual repository handling this request
     * @param context      The request context
     * @param repoPath     The request repo path
     * @param repositories List of resolution repositories
     * @return
     */
    @Nullable
    RepoResource interceptGetInfo(VirtualRepo virtualRepo, InternalRequestContext context, RepoPath repoPath,
            List<RealRepo> repositories);

    /**
     * Called after a resource is resolved and before it is returned to the client. Implementation should never
     * return null, if this kind of resource is not handled by the interceptor it should return the original.
     *
     * @param virtualRepo The virtual repository handling this request
     * @param context     The request context
     * @param resource    The <b>found</b> resource
     * @return Modified resource or original if not handled by this interceptor
     */
    @Nonnull
    RepoResource onBeforeReturn(VirtualRepo virtualRepo, InternalRequestContext context, RepoResource resource);
}
